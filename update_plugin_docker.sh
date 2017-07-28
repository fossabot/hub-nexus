#!/bin/sh
# run this from the hub-nexus project root
CURRENT_DIR=`pwd`
DATA_DIR=$CURRENT_DIR/test_data/sonatype/nexus

run() {
    echo "Starting Nexus Plugin Update..."
    build
    unzip_bundle
    docker_initialization
    copy_to_docker
    restart_container
    echo "Nexus Plugin Update: SUCCESS"
    echo
}

check_for_error() {
    local return_code=$1
    if [ "$return_code" -eq "0" ]; then
        echo "Nexus Plugin Update: FAILED"
        exit 1
    fi
}

build() {
    echo "##### Building plugin bundle"
    mvn clean install
    local mvn_return=$?
    echo $mvn_return
    if [ "$mvn_return" -eq "0" ]; then
        echo "   Build complete"
    else
        check_for_error 0
    fi
}

unzip_bundle() {
    echo "##### Unzip bundle"
    if ls target/hub-nexus*-bundle.zip 1> /dev/null 2>&1; then
        local zip_path=`ls target/hub-nexus*-bundle.zip`;
        unzip $zip_path -d target/
        return 1
    else
        echo "    Bundle zip file not found"
        return 0
    fi
}

docker_initialization() {
    echo "##### Initializing docker image and container"
    create_nexus_data_directory
    pull_container
    start_container
}

create_nexus_data_directory() {
    if [ -d $DATA_DIR ]; then
        echo "    Nexus container data directory $DATA_DIR already exists"
    else
        echo "    Creating Nexus container data directory $DATA_DIR"
        mkdir $DATA_DIR
    fi
}

pull_container() {
    echo "    Checking if nexus image pulled"
    check_nexus_image
    local return_code=$?
    if [ "$return_code" -eq "0" ]; then
        echo "        Pulling docker images..."
        docker pull sonatype/nexus
    else
        echo "        Nexus image already pulled..."
    fi
}

start_container() {
    echo "    Starting container...."
    echo "        Check if container image exists..."
    check_nexus_image
    local return_code=$?
    check_for_error $return_code
    check_nexus_container
    return_code=$?
    if [ "$return_code" -eq "0" ]; then
        echo "        Container not running... starting container"
        docker run -d -p 8081:8081 --user root:root --name nexus -v $DATA_DIR:/sonatype-work sonatype/nexus
    else
        echo "        Container is already running... no need to start"
    fi
}

check_nexus_image() {
    local image_name=$(docker images --format "{{.Repository}}" sonatype/nexus)
    if [ -z $image_name ]; then
        return 0
    else
        return 1
    fi
}

check_nexus_container() {
    is_container_running
    local container_running=$?
    if [ "$container_running" -eq "1" ]; then
        echo "    Container 'nexus' found..."
        return 1
    else
        echo "    Container 'nexus' not found..."
        return 0
    fi
}

is_container_running() {
    local container_name=$(docker ps --format "{{.Names}}" --filter name=nexus)
    if [ ! -z $container_name ]; then
        return 1
    else
        return 0
    fi
}

copy_to_docker() {
    echo "##### Copying files to docker container"
    if ls target/hub-nexus-*/ 1> /dev/null 2>&1; then
        cd target/hub-nexus-*/
        local source_path=`pwd`
        local dest_path='/opt/sonatype/nexus/nexus/WEB-INF/plugin-repository/'
        echo "    Begin copying plugin to docker:"
        echo "        Source Path:      $source_path"
        echo "        Destination Path: $dest_path"
        docker cp $source_path nexus:$dest_path
        echo "        Updating permissions and ownership"
        docker exec -it nexus bash -c "chmod -R 755 /opt/sonatype/nexus/nexus/WEB-INF/plugin-repository/hub-nexus-*/"
        docker exec -it nexus bash -c "chown root:root /opt/sonatype/nexus/nexus/WEB-INF/plugin-repository/hub-nexus-*/"
        list_installed_plugins
        list_nexus_plugin
    else
        echo "    Bundle was not unzipped"
        check_for_error 0
    fi
}

list_installed_plugins() {
    echo
    echo "    Listing Installed Plugins:"
    docker exec -it nexus bash -c "ls -al /opt/sonatype/nexus/nexus/WEB-INF/plugin-repository"
}

list_nexus_plugin() {
    echo
    echo "    Display hub-nexus Plugin Details"
    echo
    docker exec -it nexus bash -c "ls -al /opt/sonatype/nexus/nexus/WEB-INF/plugin-repository | grep hub-nexus"
}

restart_container() {
    echo "##### Restarting container 'nexus' "
    docker restart nexus
    sleep 5
    echo "    Finshed restart (you may need to wait longer for the web server to start)"
}

run
