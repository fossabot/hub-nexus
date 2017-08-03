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
Sonatype.repoServer.GovernanceInformationPanel = function(config) {

	Ext.apply(this, config || {}, {
		halfSize : false
	});

	this.sp = Sonatype.lib.Permissions;

	this.linkDivId = Ext.id();
	this.linkLabelId = Ext.id();
	Sonatype.repoServer.GovernanceInformationPanel.superclass.constructor.call( this, {
		title : 'Governance Information',
		autoScroll : true,
		border : true,
		frame : true,
		collapsible : false,
		collapsed : false,
		layout : 'form',
		labelWidth : 140,
		items: [ 
		        {
		        	html:'<img src="static/bd_logo.png" align= "right" />'
		        }, {
		        	xtype : 'displayfield',
		        	fieldLabel : 'Approval Status',
		        	name : 'approvalStatus',
		        	anchor : Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
		        	allowBlank : true,
		        	readOnly : true    	     

		        }, {
		        	xtype : 'displayfield',
		        	fieldLabel : 'Component Name',
		        	name : 'compName',
		        	anchor : Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
		        	allowBlank : true,
		        	readOnly : true
		        }, {
		        	xtype : 'panel',
		        	anchor : Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
		        	layout : 'column',
		        	frame : false,
		        	border : false,
		        	items: [
		        	        {   columnWidth: .7, 
		        	        	border: false, 
		        	        	layout: "form", 
		        	        	labelWidth : 140,
		        	        	items: [
		        	        	        {
		        	        	        	xtype : 'displayfield',
		        	        	        	fieldLabel : 'Component Version',
		        	        	        	name : 'compVersion',
		        	        	        	anchor : Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
		        	        	        	allowBlank : true,
		        	        	        	readOnly : true
		        	        	        }
		        	        	        ]
		        	        },{ columnWidth: .3, 
		        	        	border: false, 
		        	        	layout: "form", 
		        	        	items: [
		        	        	        {
		        	        	        	xtype : 'displayfield',
		        	        	        	name : 'codeCenterLink',
		        	        	        	fieldLabel : null,
		        	        	        	hideLabel : true,
		        	        	        	anchor : Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
		        	        	        	allowBlank : true,
		        	        	        	readOnly : true,
		        	        	        	cls : 'cc_link'

		        	        	        }
		        	        	        ]
		        	        }
		        	        ]
		        }, {
		        	xtype : 'displayfield',
		        	fieldLabel : 'Component Type',
		        	name : 'compType',
		        	anchor : Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
		        	allowBlank : true,
		        	readOnly : true
		        }, {
		        	xtype : 'displayfield',
		        	fieldLabel : 'Homepage',
		        	name : 'homePage',
		        	anchor : Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
		        	allowBlank : true,
		        	readOnly : true
		        }, {
		        	xtype : 'displayfield',
		        	fieldLabel : 'Declared License',
		        	name : 'declaredLicense',
		        	anchor : Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
		        	allowBlank : true,
		        	readOnly : true
		        }, {
		        	xtype : 'displayfield',
		        	fieldLabel : 'Alternate License(s)',
		        	name : 'alternateLicense',
		        	anchor : Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
		        	allowBlank : true,
		        	readOnly : true
		        }, {
		        	xtype : 'displayfield',
		        	fieldLabel : 'Component Description',
		        	name : 'compDescription',
		        	anchor : Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
		        	allowBlank : true,
		        	readOnly : true,
		        	height: 50,
		        	cls : 'comp_Description'

		        }, {
		        	xtype : 'displayfield',
		        	fieldLabel : 'Message',
		        	name : 'error',
		        	anchor : Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
		        	allowBlank : true,
		        	readOnly : true
		        }, {
		        	xtype : 'panel',
		        	anchor : Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
		        	layout : 'column',
		        	cls: 'count-panel',
		        	frame : false,
		        	border : false,
		        	items: [
		        	        {   
		        	        	columnWidth: .33, 
		        	        	border: false, 
		        	        	layout: "form",
		        	        	labelWidth: 50,
		        	        	items: [
		        	        	        {
		        	        	        	xtype : 'displayfield',
		        	        	        	name : 'highVulnerabilityCount',
		        	        	        	fieldLabel : 'High',
		        	        	        	anchor : Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
		        	        	        	allowBlank : true,
		        	        	        	readOnly : true,
		        	        	        	labelStyle : 'max-width: 50px; font-family: arial, helvetica, clean, sans-serif; font-size: 12px; font-weight: bold; vertical-align: middle; padding-top: 5px; padding-right: 2px; margin-bottom: 10px; display: inline-block; white-space: nowrap; cursor: pointer; text-decoration:underline;',
		        	        	        	cls : 'vuln-high-count',
		        	        	        	listeners: {
		        	        	        		render : function(field){
		        	        	        			var label = field.ownerCt.el.child(".x-form-item-label");
		        	        	        			label.on("click", countFiltering,  this, this.ownerCt.ownerCt.ownerCt);
		        	        	        		},
		        	        	        		resize : function(field, adjWidth, adjHeight, rawWidth, rawHeight ){
		        	        	        			var width = field.ownerCt.body.dom.style.width;
		        	        	        			//Remove the px at the end of the string
		        	        	        			width = width.substr(0, width.length-2);

		        	        	        			//Change to int
		        	        	        			width = parseInt(width, 10);

		        	        	        			if(width > 130){
		        	        	        				field.ownerCt.body.dom.style.width = '130px';
		        	        	        			}
		        	        	        		}

		        	        	        	}
		        	        	        }
		        	        	        ]
		        	        },{ 
		        	        	columnWidth: .33, 
		        	        	border: false, 
		        	        	layout: "form", 
		        	        	labelWidth: 50,
		        	        	items: [
		        	        	        {
		        	        	        	xtype : 'displayfield',
		        	        	        	name : 'mediumVulnerabilityCount',
		        	        	        	fieldLabel : 'Medium',
		        	        	        	anchor : Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
		        	        	        	allowBlank : true,
		        	        	        	readOnly : true,
		        	        	        	labelStyle : 'max-width: 50px; font-family: arial, helvetica, clean, sans-serif; font-size: 12px; font-weight: bold; vertical-align: middle; padding-top: 5px; padding-right: 2px; margin-bottom: 10px; display: inline-block; white-space: nowrap; cursor: pointer; text-decoration:underline;',
		        	        	        	cls : 'vuln-med-count',
		        	        	        	listeners: {
		        	        	        		render : function(field){
		        	        	        			var label = field.ownerCt.el.child(".x-form-item-label");
		        	        	        			label.on("click", countFiltering, this, this.ownerCt.ownerCt.ownerCt);
		        	        	        		},
		        	        	        		resize : function(field, adjWidth, adjHeight, rawWidth, rawHeight ){
		        	        	        			var width = field.ownerCt.body.dom.style.width;
		        	        	        			//Remove the px at the end of the string
		        	        	        			width = width.substr(0, width.length-2);

		        	        	        			//Change to int
		        	        	        			width = parseInt(width, 10);

		        	        	        			if(width > 130){
		        	        	        				field.ownerCt.body.dom.style.width = '130px';
		        	        	        			}
		        	        	        		}

		        	        	        	}

		        	        	        }
		        	        	        ]
		        	        },{ 
		        	        	columnWidth: .33, 
		        	        	border: false, 
		        	        	layout: "form", 
		        	        	labelWidth: 50,
		        	        	items: [
		        	        	        {
		        	        	        	xtype : 'displayfield',
		        	        	        	fieldLabel : 'Low',
		        	        	        	name : 'lowVulnerabilityCount',
		        	        	        	anchor : Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
		        	        	        	allowBlank : true,
		        	        	        	readOnly : true,
		        	        	        	labelStyle : 'max-width: 50px; font-family: arial, helvetica, clean, sans-serif; font-size: 12px; font-weight: bold; vertical-align: middle; padding-top: 5px; padding-right: 2px; margin-bottom: 10px; display: inline-block; white-space: nowrap; cursor: pointer; text-decoration:underline;',
		        	        	        	cls : 'vuln-low-count',
		        	        	        	listeners: {
		        	        	        		render : function(field){
		        	        	        			var label = field.ownerCt.el.child(".x-form-item-label");
		        	        	        			label.on("click", countFiltering,  this, this.ownerCt.ownerCt.ownerCt);
		        	        	        		},
		        	        	        		resize : function(field, adjWidth, adjHeight, rawWidth, rawHeight ){
		        	        	        			var width = field.ownerCt.body.dom.style.width;
		        	        	        			//Remove the px at the end of the string
		        	        	        			width = width.substr(0, width.length-2);

		        	        	        			//Change to int
		        	        	        			width = parseInt(width, 10);

		        	        	        			if(width > 130){
		        	        	        				field.ownerCt.body.dom.style.width = '130px';
		        	        	        			}
		        	        	        		}

		        	        	        	}
		        	        	        }
		        	        	        ]
		        	        }

		        	        ]
		        }, {
		        	xtype : 'grid',
		        	name : 'vulnerabilityGrid',
		        	anchor : Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
		        	allowBlank : true,
		        	readOnly : true,
		        	autoScroll: true,
		        	title: 'Vulnerabilities',
		        	collapsible: true,
		        	enableColumnResize: true,
		        	autoExpandColumn: 'descriptionId',
		        	store: new Ext.data.Store({
		        		autoDestroy: true,
		        		storeId: 'vulnerabilityStore',
		        		reader: new Ext.data.ArrayReader({
		        			fields : ["vulName", "vulDate", "vulSeverity",  "vulDescription"]
		        		})
		        	}),
		        	columns: [{
		        		header: 'Name',
		        		dataIndex: 'vulName',
		        		width: 100,
		        		sortable: true,
		        		id: 'nameId',
		        		editable: false,
		        		renderer: function(value, metaData, record, rowIndex, colIndex, store) {
		        			var myURL = '';
		        			if(value !== ''){
		        				myURL = '<a href="http://web.nvd.nist.gov/view/vuln/detail?vulnId=' + value + '" target="_blank">' + value +'</a>';
		        			}
		        			return myURL;
		        		}
		        	}, {
		        		header: 'Date',
		        		dataIndex: 'vulDate',
		        		width: 80,
		        		sortable: true,
		        		id: 'dateId',
		        		editable: false
		        	}, {
		        		header: 'Severity',
		        		dataIndex: 'vulSeverity',
		        		width: 60,
		        		sortable: true,
		        		id: 'severityId',
		        		align: 'center',
		        		editable: false,
		        		resizable: false
		        	}, {
		        		header: 'Description',
		        		dataIndex: 'vulDescription',
		        		sortable: false,
		        		id: 'descriptionId',
		        		editable: false
		        	}],
		        	height: 150,
		        	listeners: {
		        		render: function (grid) {
		        			var view = grid.getView();			
		        			grid.toolTip = new Ext.ToolTip({
		        				target: view.el,
		        				delegate: view.cellSelector,
		        				constrainPosition: true,
		        				trackMouse: false,
		        				dismissDelay: 0,
		        				renderTo: Ext.getBody(),
		        				listeners: {
		        					beforeshow: function updateTipBody(tip) {
		        						//Clear the tooltip text from previous cells
		        						tip.body.dom.textContent = '';
		        						var view = grid.getView();
		        						var store = grid.getStore();
		        						var columnIndex = view.findCellIndex(tip.triggerElement);
		        						var row = view.findRow(tip.triggerElement);
		        						if(row != null){
		        							var data = view.getCell(row.rowIndex, columnIndex);
		        							tip.body.dom.textContent = data.textContent;
		        						}				
		        					}
		        					}
		        				
		        			});
		        		}
		        	}


		        }, {
		        	xtype : 'displayfield',
		        	fieldLabel : 'ExternalId',
		        	name : 'externalId',
		        	anchor : Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
		        	allowBlank : true,
		        	readOnly : true
		        }
		        ]

	});
};

Ext.extend( Sonatype.repoServer.GovernanceInformationPanel, Ext.Panel, {
	showArtifact : function(data, artifactContainer) {
		noData(this);
		showBasicMetaData(this);
		var vulnerabilityStore = this.find('name', 'vulnerabilityGrid')[0].store;
		//remove all previous entries, this way we dont carry over old information
		vulnerabilityStore.removeAll();
		var VulnerabilityRecord = Ext.data.Record.create(
				[
				 {name: 'vulName', mapping: 'name'},
				 {name: 'vulDate', mapping: 'date'},
				 {name: 'vulSeverity', mapping: 'severity'},
				 {name: 'vulDescription', mapping: 'description'},
				 ]);
		this.data = data;
		if (data) {
			Ext.Ajax.request({
				url : this.data.resourceURI,
				callback : function(options, isSuccess, response) {
					if (isSuccess) {
						var infoResp = Ext.decode(response.responseText);

						this.find('name', 'externalId')[0].setRawValue(infoResp.data.externalId);
						this.find('name', 'error')[0].setRawValue(infoResp.data.error);
						if(infoResp.data.error || infoResp.data.error != ""){
							this.find('name', 'error')[0].show();
						} else{
							this.find('name', 'error')[0].hide();
						}

						if(infoResp.data.error || infoResp.data.error != ""){
							hideAll(this);

						} else{
							showBasicMetaData(this);

							//Only show approval status for catalog components, hide it if it has no value
							if(infoResp.data.approvalStatus && infoResp.data.approvalStatus != ""){
								this.find('name', 'approvalStatus')[0].show();
							} else{
								this.find('name', 'approvalStatus')[0].hide();
							}

							this.find('name', 'compName')[0].setRawValue(infoResp.data.compName);
							this.find('name', 'compVersion')[0].setRawValue(infoResp.data.compVersion);

							if(infoResp.data.componentLink.indexOf("undefined") > -1){
								this.find('name', 'codeCenterLink')[0].hide();
							}else{
								this.find('name', 'codeCenterLink')[0].setRawValue('<a href="' + infoResp.data.componentLink + '" target="_blank"> Code Center <i class="fa fa-share-square-o"></i></a>');
							}

							this.find('name', 'compType')[0].setRawValue(infoResp.data.compType);
							if(infoResp.data.homePage == "undefined"){
								this.find('name', 'homePage')[0].setRawValue(infoResp.data.homePage);
							} else{
								this.find('name', 'homePage')[0].setRawValue('<a href="' + infoResp.data.homePage + '" target="_blank">' + infoResp.data.homePage +'</a>');
							}
							this.find('name', 'declaredLicense')[0].setRawValue(infoResp.data.declaredLicense);
							
							this.find('name', 'alternateLicense')[0].setRawValue(infoResp.data.alternateLicenses);
							
							if(infoResp.data.alternateLicenses && infoResp.data.alternateLicenses != ""){
								this.find('name', 'alternateLicense')[0].show();
							} else{
								this.find('name', 'alternateLicense')[0].hide();
							}

							this.find('name', 'compDescription')[0].setRawValue(infoResp.data.compDescription);

							this.find('name', 'lowVulnerabilityCount')[0].setRawValue(infoResp.data.lowVulnerabilityCount);
							this.find('name', 'mediumVulnerabilityCount')[0].setRawValue(infoResp.data.mediumVulnerabilityCount);
							this.find('name', 'highVulnerabilityCount')[0].setRawValue(infoResp.data.highVulnerabilityCount);


							if(infoResp.data.totalVulnerabilityCount > 0){
								this.find('name', 'vulnerabilityGrid')[0].expand();
								for(var i = 0; i < infoResp.data.totalVulnerabilityCount; i=i+1){
									var vulnerabiliyInfo = infoResp.data.vulnerabilities[i]
									var newVulnerabilityRecord = new VulnerabilityRecord(
											{
												vulName: vulnerabiliyInfo[0],
												vulDate: vulnerabiliyInfo[1],
												vulDescription: vulnerabiliyInfo[2],
												vulSeverity: vulnerabiliyInfo[3]

											});
									vulnerabilityStore.add(newVulnerabilityRecord);
								}
							} else{
								this.find('name', 'vulnerabilityGrid')[0].collapse();
							}
						}
						artifactContainer.showTab(this);



					} else {
						if (response.status === 404) {
							artifactContainer.hideTab(this);
						}
						else {
							Sonatype.utils.connectionError(response, 'Unable to retrieve Governance information.');
						}
					}
				},
				//Adding more parameters will cause issues, you will have to change the parseForVersion method in BDArtifactMetadata if you add more here
				params : {
					describe : 'blackduck',
					isLocal : true,
					groupId : data.groupId,
					artifactId : data.artifactId,
					version : data.version,
				},
				scope : this,
				method : 'GET',
				suppressStatus : 404
			});		

		}
	}
});

var currentLabelFilter = null

function countFiltering(event, label, governanceTab){
	var vulnerabilityStore = governanceTab.find('name', 'vulnerabilityGrid')[0].store;
	if(vulnerabilityStore.isFiltered()){
		if (currentLabelFilter != null){
			currentLabelFilter.style.backgroundColor = '';
			currentLabelFilter.style.color = '';
			currentLabelFilter.style.borderRadius = '';
			currentLabelFilter.style.border= '';
			currentLabelFilter = null;
		}
		vulnerabilityStore.clearFilter();
	} else{
		currentLabelFilter = label;
		currentLabelFilter.style.color = 'darkRed';
		currentLabelFilter.style.backgroundColor = 'rgba(255,238,0,.4)';
		currentLabelFilter.style.borderRadius = '3px';
		currentLabelFilter.style.border= '1px solid #FFB900';

		if(label.textContent == "Low:"){
			vulnerabilityStore.filter('vulSeverity', "Low", false, false);
		} else if(label.textContent == "Medium:"){
			vulnerabilityStore.filter('vulSeverity', "Medium", false, false);
		} else{
			vulnerabilityStore.filter('vulSeverity', "High", false, false);
		}
	}
}

Sonatype.Events.addListener('fileContainerInit', function(items) {
	items.push(new Sonatype.repoServer.GovernanceInformationPanel({
		name : 'governanceInformationPanel',
		tabTitle : 'Governance Information',
		preferredIndex : 10
	}));
});

Sonatype.Events.addListener('fileContainerUpdate', function(artifactContainer, data) {
	var panel = artifactContainer.find('name', 'governanceInformationPanel')[0];

	if (data && data.leaf) {
		panel.showArtifact(data, artifactContainer);
	}
	else {
		panel.showArtifact(null, artifactContainer);
	}
});

Sonatype.Events.addListener('artifactContainerInit', function(items) {
	items.push(new Sonatype.repoServer.GovernanceInformationPanel({
		name : 'governanceInformationPanel',
		tabTitle : 'Governance Information',
		preferredIndex : 10
	}));
});

Sonatype.Events.addListener('artifactContainerUpdate', function(artifactContainer, payload) {
	var panel = artifactContainer.find('name', 'governanceInformationPanel')[0];

	if (payload && payload.leaf) {
		panel.showArtifact(payload, artifactContainer);
	}
	else {
		panel.showArtifact(null, artifactContainer);
	}

});