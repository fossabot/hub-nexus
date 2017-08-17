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
		        },{
		        	xtype : 'displayfield',
		        	fieldLabel : 'UI URL',
		        	name : 'uiUrl',
		        	anchor : Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
		        	allowBlank : true,
		        	readOnly : true    	     

		        }, {
		        	xtype : 'hidden',
		        	fieldLabel : 'API URL',
		        	name : 'apiUrl',
		        	anchor : Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
		        	allowBlank : true,
		        	readOnly : true    	     

		        }, {
		        	xtype : 'displayfield',
		        	fieldLabel : 'Overall Policy Status',
		        	name : 'overallPolicyStatus',
		        	anchor : Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
		        	allowBlank : true,
		        	readOnly : true
		        }, {
		        	xtype : 'displayfield',
		        	fieldLabel : 'Policy Status',
		        	name : 'policyStatus',
		        	anchor : Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
		        	allowBlank : true,
		        	readOnly : true
		        }, {
		        	xtype : 'displayfield',
		        	fieldLabel : 'Scan Time',
		        	name : 'scanTime',
		        	anchor : Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
		        	allowBlank : true,
		        	readOnly : true
		        }, {
		        	xtype : 'displayfield',
		        	fieldLabel : 'Scan Result',
		        	name : 'scanResult',
		        	anchor : Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
		        	allowBlank : true,
		        	readOnly : true
		        }
		        
		    ]
	});
};

Ext.extend( Sonatype.repoServer.HubTab, Ext.Panel, {
	showArtifact : function(data, artifactContainer) {
		var currentUri = data.resourceURI;
		var indexOfNexus = currentUri.indexOf('/nexus')
		currentUri = currentUri.slice(indexOfNexus, currentUri.length);
		var urlSeg = currentUri.split("/");
		var repoId = urlSeg[5];
		var newArr = urlSeg.slice(7, urlSeg.length);
		var artPath = '/' + newArr.join('/');
		var self = this;
		this.data = data;
		if (data != null) {
			Ext.Ajax.request({
				url : '/nexus/service/siesta/blackduck/info?repoId=' + repoId + '&itemPath=' + artPath,
				callback : function(options, isSuccess, response) {
					if (isSuccess) {
						var infoResp = Ext.decode(response.responseText);
						showBasicMetaData(self);

						if(infoResp.scanTime == '0') {
							artifactContainer.hideTab(this);
						} else {

							var dateTime = Date(parseInt(infoResp.scanTime));
							dateTime = dateTime.toLocaleString();
							
							var uiUrl = '<a href="' + infoResp.uiUrl + '" target="_blank">' + infoResp.uiUrl + '</a>';

							this.find('name', 'uiUrl')[0].setRawValue(uiUrl);
							this.find('name', 'overallPolicyStatus')[0].setRawValue(infoResp.policyOverallStatus);
							this.find('name', 'policyStatus')[0].setRawValue(infoResp.policyStatus);
							this.find('name', 'scanTime')[0].setRawValue(dateTime);
							this.find('name', 'scanResult')[0].setRawValue(infoResp.scanStatus);

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
	hubTab.find('name', 'scanTime')[0].setRawValue(null);
	hubTab.find('name', 'apiUrl')[0].setRawValue(null);
	hubTab.find('name', 'policyStatus')[0].setRawValue(null);
}

function showBasicMetaData(hubTab){
	hubTab.find('name', 'apiUrl')[0].show();
	hubTab.find('name', 'scanTime')[0].show();
	hubTab.find('name', 'policyStatus')[0].show();
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