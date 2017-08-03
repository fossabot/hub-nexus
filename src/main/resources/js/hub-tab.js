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
// define('Sonatype/repoServer/hub-tab', function() {
//   Ext.form.TestField = Ext.extend(Ext.form.DisplayField, {
//     setValue : function() {
//         return 'test';
//     }
//   });

//   Ext.reg('testField', Ext.form.TestField);

//   Sonatype.repoServer.HubTab = function(config) {
//     var config = config || {};
//     var defaultConfig = {};
//     Ext.apply(this, config, defaultConfig);

//     Sonatype.repoServer.HubTab.superclass.constructor.call(this, {
//           title : 'New tab',
//           autoScroll : true,
//           border : true,
//           frame : true,
//           collapsible : false,
//           collapsed : false,
//           items : [{
//                 xtype : 'fieldset',
//                 checkboxToggle : false,
//                 title : 'Test set',
//                 anchor : Sonatype.view.FIELDSET_OFFSET,
//                 collapsible : false,
//                 autoHeight : true,
//                 layoutConfig : {
//                   labelSeparator : ''
//                 },
//                 items : [{
//                       xtype : 'panel',
//                       items : [{
//                             xtype : 'testField',
//                             name : 'tesssstt',
//                             anchor : Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
//                             allowBlank : true,
//                             readOnly : true
//                           }]
//                     }]
//               }]
//         });
//   };
//     Sonatype.Events.addListener('fileContainerInit', function(items) {
//         items.push(new Sonatype.repoServer.HubTab({
//               name : 'hub-tab',
//               tabTitle : 'Newtab',
//               preferredIndex : 30
//             }));
//       });

//         Sonatype.Events.addListener('artifactContainerInit', function(items) {
//         items.push(new SSonatype.repoServer.HubTab({
//               name : 'hub-tab',
//               tabTitle : 'New tab',
//               preferredIndex : 30
//             }));
//       });
// });

define('Sonatype/repoServer/hub-tab', function() {
Sonatype.repoServer.HubTab = function (config) {
    var config = config || {};
    var defaultConfig = {};
    Ext.apply(this, config, defaultConfig);

    Sonatype.repoServer.HubTab.superclass.constructor.call(this, {
        title: 'test',
        autoScroll: true,
        border: true,
        frame: true,
        collapsible: false,
        collapsed: false,
        items : [{
            xtype : 'fieldset',
            checkboxToggle : false,
            title : 'Test set',
            anchor : Sonatype.view.FIELDSET_OFFSET,
            collapsible : false,
            autoHeight : true,
            layoutConfig : {
                  labelSeparator : ''
            },
            items : [{
                xtype : 'panel',
                items : [{
                    xtype : 'testField',
                    name : 'tesssstt',
                    anchor : Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
                    allowBlank : true,
                    readOnly : true
                }]
            }]
        }]
    });
};
});