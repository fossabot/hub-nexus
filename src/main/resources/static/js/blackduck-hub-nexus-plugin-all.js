/*
 * hub-nexus
 *
 * 	Copyright (C) 2018 Black Duck Software, Inc.
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
	var globalTabData = '';

	Ext.apply(this, config || {}, {
		halfSize : false
	});

	this.sp = Sonatype.lib.Permissions;

	Sonatype.repoServer.HubTab.superclass.constructor.call( this, {
		title : 'Black Duck Hub',
		autoScroll : true,
		border : true,
		frame : true,
		collapsible : false,
		collapsed : false,
		labelWidth : 130,
		layout : 'form',
		items: [ 
		        {
		        	html:'<img src="static/icons/bd_logo.png" align= "right" />'
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
		        	readOnly : true,    	     
		        }, {
		        	xtype : 'displayfield',
		        	fieldLabel : 'Overall Policy Status',
		        	name : 'overallPolicyStatus',
		        	anchor : Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
		        	allowBlank : true,
		        	readOnly : true,    	     
		        }, {
		        	xtype : 'displayfield',
		        	fieldLabel : 'Policy Status',
		        	name : 'policyStatus',
		        	anchor : Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
		        	allowBlank : true,
		        	readOnly : true,    	     
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
		        }, {
					xtype : 'button',
					text : 'Clear artifact attributes',
					name : 'buttonClearAttributes',
					handler : this.clearAttributesHandler,
				}
		        
		    ]
	});
};

Ext.extend(Sonatype.repoServer.HubTab, Ext.Panel, {
	clearAttributesHandler : function() {
		Sonatype.MessageBox.show({
			title : 'Confirmation',
			msg : 'Deleting the artifact info will remove this hub data from nexus and cause the artifact to be scanned again in the future, are you sure you want to continue?',
			buttons : Sonatype.MessageBox.YESNO,
			scope : this,
			icon : Sonatype.MessageBox.QUESTION,
			fn : function(btn) {
				if(btn == 'ok' || btn == 'yes') {
					var repoId = globalTabData.repoId;
					var currentUri = globalTabData.resourceURI;
					var indexOfNexus = currentUri.indexOf('/nexus');
					currentUri = currentUri.slice(indexOfNexus, currentUri.length);
					var urlSeg = currentUri.split("/");
					var newArr = urlSeg.slice(7, urlSeg.length);
					var artPath = '/' + newArr.join('/');

					Ext.Ajax.request({
						url : '/nexus/service/siesta/blackduck/info?repoId=' + repoId + '&itemPath=' + artPath,
						method : 'DELETE',
						scope : this,
						callback : function(options, isSuccess, response) {
							if(isSuccess) {
								var panel = Sonatype.view.mainTabPanel.getActiveTab();
								var id = panel.getId();
								if(id == 'view-repositories') {
									panel.refreshHandler(null, null);
								}
							}
						}
					});
				} else {
					return;
				}
			}
		});
	},
	showArtifact : function(data, artifactContainer) {
		globalTabData = data;
		
		var repoId = data.repoId
		var currentUri = data.resourceURI;
		var indexOfNexus = currentUri.indexOf('/nexus')
		currentUri = currentUri.slice(indexOfNexus, currentUri.length);
		var urlSeg = currentUri.split("/");
		var newArr = urlSeg.slice(7, urlSeg.length);
		var artPath = '/' + newArr.join('/');
		if (data != null) {
			Ext.Ajax.request({
				url : '/nexus/service/siesta/blackduck/info?repoId=' + repoId + '&itemPath=' + artPath,
				callback : function(options, isSuccess, response) {
					if (isSuccess) {
						var infoResp = Ext.decode(response.responseText);

						if (infoResp.scanTime == '0') {
							artifactContainer.hideTab(this);
						} else {
							var dateTime = new Date(parseInt(infoResp.scanTime));
							dateTime = dateTime.toLocaleString();
							
							var uiUrl = '<a href="' + infoResp.uiUrl + '" target="_blank">' + infoResp.uiUrl + '</a>';

							this.find('name', 'uiUrl')[0].setRawValue(uiUrl);
							this.find('name', 'overallPolicyStatus')[0].setRawValue(infoResp.policyOverallStatus);
							this.find('name', 'policyStatus')[0].setRawValue(infoResp.policyStatus);
							this.find('name', 'scanTime')[0].setRawValue(dateTime);
							var scanStatus = infoResp.scanStatus;
							var statusString = "Failed";
							if (scanStatus == '1') {
								statusString = "Success";
							} 
							this.find('name', 'scanResult')[0].setRawValue(statusString);

							artifactContainer.showTab(this);
						}
					} else {
						if (response.status = 404) {
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
			artifactContainer.hideTab(this);
		}
	}
});

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