/*
 * hub-nexus
 *
 * 	Copyright (C) 2017 Black Duck Software, Inc.
 * 	http://www.blackducksoftware.com/
 *
 * 	Licensed to the Apache Software Foundation (ASF) under one
 * 	or more contributor license agreements. See the NOTICE file
 * 	distributed with this work for additional information
 * 	regarding copyright ownership. The ASF licenses this file
 * 	to you under the Apache License, Version 2.0 (the
 * 	"License"); you may not use this file except in compliance
 * 	with the License. You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * 	Unless required by applicable law or agreed to in writing,
 * 	software distributed under the License is distributed on an
 * 	"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * 	KIND, either express or implied. See the License for the
 * 	specific language governing permissions and limitations
 * 	under the License.
 */
Sonatype.repoServer.HubTab = function(config) {

	Ext.apply(this, config || {}, {
		halfSize : false
	});

	this.sp = Sonatype.lib.Permissions;

	this.servicePath = {
		connectionInfo: Sonatype.config.servicePath + '/blackduck',
	};

	Sonatype.repoServer.HubTab.superclass.constructor.call( this, {
		title : 'Hub',
		autoScroll : true,
		border : true,
		frame : true,
		collapsible : false,
		collapsed : false,
		layout : 'form',
		items: [ 
		        {
		        	html:'<img src="static/bd_logo.png" align= "right" />'
		        }, {
		        	xtype : 'displayfield',
		        	fieldLabel : 'Risk report URL',
		        	name : 'riskReportUrl',
		        	anchor : Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
		        	allowBlank : true,
		        	readOnly : true    	     

		        }, {
		        	xtype : 'displayfield',
		        	fieldLabel : 'Policy check status',
		        	name : 'policyResult',
		        	anchor : Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
		        	allowBlank : true,
		        	readOnly : true
		        }, {
		        	xtype : 'displayfield',
		        	fieldLabel : 'Last scanned',
		        	name : 'lastScanned',
		        	anchor : Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
		        	allowBlank : true,
		        	readOnly : true
		        }
		    ]
	});
};

Ext.extend( Sonatype.repoServer.HubTab, Ext.Panel, {
	showArtifact : function(data, artifactContainer) {
		var self = this;
		this.data = data;
		if (data != null) {
			Ext.Ajax.request({
				url : this.data.resourceURI + '?describe',
				callback : function(options, isSuccess, response) {
					if (isSuccess) {
						var infoResp = Ext.decode(response.responseText);
						showBasicMetaData(self);

						var filteredArray = infoResp.data.response.attributes.filter(item => item.includes('blackducksoftware-'));

						if(filteredArray.length == 0) {
							artifactContainer.hideTab(this);
						} else {
							for (let index of filteredArray) {
								var keyAndValue = index.split("=");
								var key = keyAndValue[0].split("-")[1];
								var value = keyAndValue[1];
								
								var dateTime;

								if(key == 'lastScanned') {
									dateTime = Date(parseInt(value));
									value = dateTime.toLocaleString();
								}
								
								if(key == 'riskReportUrl') {
									value = '<a href="' + value + '" target="_blank">' + value + '</a>';
								} 
								
								this.find('name', key)[0].setRawValue(value);
							}

							artifactContainer.showTab(this);
						}
					} else {
						if(response.status = 404) {
							artifactContainer.hideTab(this);
						}
					}
				},
				scope : this,
				method : 'GET',
				suppressStatus : '404'
			});
		} else {
			console.log('data is null');
			noData(this);
			artifactContainer.hideTab(this);
		}
	}
});

function noData(hubTab){
	hubTab.find('name', 'lastScanned')[0].setRawValue(null);
	hubTab.find('name', 'riskReportUrl')[0].setRawValue(null);
	hubTab.find('name', 'policyResult')[0].setRawValue(null);
}

function showBasicMetaData(hubTab){
	hubTab.find('name', 'riskReportUrl')[0].show();
	hubTab.find('name', 'lastScanned')[0].show();
	hubTab.find('name', 'policyResult')[0].show();
}

Sonatype.Events.addListener('fileContainerInit', function(items) {
	items.push(new Sonatype.repoServer.HubTab({
		name : 'hubTab',
		tabTitle : 'Hub',
		preferredIndex : 30
	}));
});

Sonatype.Events.addListener('fileContainerUpdate', function(artifactContainer, data) {
	var panel = artifactContainer.find('name', 'hubTab')[0];

	if (data && data.leaf) {
		panel.showArtifact(data, artifactContainer);
	}
	else {
		panel.showArtifact(null, artifactContainer);
	}
});

Sonatype.Events.addListener('artifactContainerInit', function(items) {
	items.push(new Sonatype.repoServer.HubTab({
		name : 'hubTab',
		tabTitle : 'Hub',
		preferredIndex : 30
	}));
});

Sonatype.Events.addListener('artifactContainerUpdate', function(artifactContainer, payload) {
	var panel = artifactContainer.find('name', 'hubTab')[0];

	if (payload && payload.leaf) {
		panel.showArtifact(payload, artifactContainer);
	}
	else {
		panel.showArtifact(null, artifactContainer);
	}

});