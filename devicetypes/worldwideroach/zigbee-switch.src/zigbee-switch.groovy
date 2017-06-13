/**
 *  Copyright 2015 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */

metadata {
    definition (name: "ZigBee Switch", namespace: "worldwideroach", author: "Brian Roach", ocfDeviceType: "oic.d.switch") {
        capability "Actuator"
        capability "Configuration"
        capability "Refresh"
        capability "Switch"
        capability "Switch Level"
        capability "Health Check"
        capability "Holdable Button"
        capability "Momentary"
        capability "Bridge"
        capability "Battery"

        fingerprint profileId: "0104", inClusters: "0000, 0001, 0003, 0B05", outClusters: "0003, 0005, 0006, 0008, 0019, 0300", manufacturer: "SR", model: "ZGRC-KEY-001", deviceJoinName: "Sunricher SR-ZG9001K4-DIM"
        //fingerprint profileId: "0104", inClusters: "0000, 0001, 0003, 0B05", outClusters: "0003, 0005, 0006, 0008, 0019, 0300", manufacturer: "SR", model: "ZGRC-TUS-004", deviceJoinName: "Sunricher SR-ZG9002T3-CCT-US"
    }

    // simulator metadata
    simulator {
        // status messages
        status "on": "on/off: 1"
        status "off": "on/off: 0"

        // reply messages
        reply "zcl on-off on": "on/off: 1"
        reply "zcl on-off off": "on/off: 0"
    }

    tiles(scale: 2) {
        multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00A0DC", nextState:"turningOff"
                attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
                attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00A0DC", nextState:"turningOff"
                attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
            }
        }
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        
        main "switch"
        details(["switch", "refresh"])
    }
}

// Globals
private getUINT8() { 0x20 }

private getCLUSTER_SWITCH() { 6 }
private getCLUSTER_SWITCH_LEVEL() { 8 }
private getCLUSTER_POWER_CONFIGURATION() { 0x0001 }

private getSWITCH_ON_CLICKED() { 1 }

private getSWITCH_OFF_CLICKED() { 0 }

private getSWITCH_LEVEL_DOWN() { 5 }
private getSWITCH_LEVEL_CLICKED() { 6 }
private getSWITCH_LEVEL_UP() { 7 }

private getSWITCH_LEVEL_BRIGHTER() { '00' }
private getSWITCH_LEVEL_DIMMER() { '01' }

private getBATTERY_INFO_VOLTAGE() { 0x0020 }
private getBATTERY_INFO_PCT_REMAINING() { 0x0021 }
private getBATTERY_SETTING_MANUFACTURER() { 0x0030 }
private getBATTERY_SETTING_SIZE() { 0x0031 }
private getBATTERY_SETTING_AHR_RATING() { 0x0032 }
private getBATTERY_SETTING_QUANTITY() { 0x0033 }
private getBATTERY_SETTING_RATED_VOLTAGE() { 0x0034 }
private getBATTERY_SETTING_ALARM_MASK() { 0x0035 }
private getBATTERY_SETTING_VOLTAGE_MIN_THRESHOLD() { 0x0036 }
private getBATTERY_SETTING_VOLTAGE_THRESHOLD_1() { 0x0037 }
private getBATTERY_SETTING_VOLTAGE_THRESHOLD_2() { 0x0038 }
private getBATTERY_SETTING_VOLTAGE_THRESHOLD_3() { 0x0039 }
private getBATTERY_SETTING_PCT_MIN_THRESHOLD() { 0x003a }
private getBATTERY_SETTING_PCT_THRESHOLD_1() { 0x003b }
private getBATTERY_SETTING_PCT_THRESHOLD_2() { 0x003c }
private getBATTERY_SETTING_PCT_THRESHOLD_3() { 0x003d }
private getBATTERY_SETTING_ALARM_STATE() { 0x003e }

//def greeting = { it -> "Hello, $it!" }

// Parse incoming device messages to generate events
def parse(String description) {
	def evt = [];
    def descMap = parseReportAttributeMessage(description);
    
    def strMap = ""
    descMap.each { key, value ->
    	strMap = strMap.concat("\n" + key + " = " + value)
	}
    
    log.debug strMap
    
    switch(descMap.cluster) {
    	case "switch":
        	if (descMap.button == "on") { 
            	on();
                evt = createEvent(name: "switch", value: "on");
                //switch.on
            }
            if (descMap.button == "off") {
            	off();
                evt = createEvent(name: "switch", value: "off")
            }
        	break;
        case "switch level":
        	break;
    }
    
    readBatteryState();
    
    return evt;
}

def off() {
    zigbee.off()
}

def on() {
    zigbee.on()
}

def readBatteryState() {

    def battery20 = zigbee.readAttribute(CLUSTER_POWER_CONFIGURATION, BATTERY_INFO_VOLTAGE)
    def battery21 = zigbee.readAttribute(CLUSTER_POWER_CONFIGURATION, BATTERY_INFO_PCT_REMAINING)
    def battery30 = zigbee.readAttribute(CLUSTER_POWER_CONFIGURATION, BATTERY_SETTING_MANUFACTURER)
    def battery31 = zigbee.readAttribute(CLUSTER_POWER_CONFIGURATION, BATTERY_SETTING_SIZE)
    def battery32 = zigbee.readAttribute(CLUSTER_POWER_CONFIGURATION, BATTERY_SETTING_AHR_RATING)
    def battery33 = zigbee.readAttribute(CLUSTER_POWER_CONFIGURATION, BATTERY_SETTING_QUANTITY)
    def battery34 = zigbee.readAttribute(CLUSTER_POWER_CONFIGURATION, BATTERY_SETTING_RATED_VOLTAGE)
    def battery35 = zigbee.readAttribute(CLUSTER_POWER_CONFIGURATION, BATTERY_SETTING_ALARM_MASK)
    def battery36 = zigbee.readAttribute(CLUSTER_POWER_CONFIGURATION, BATTERY_SETTING_VOLTAGE_MIN_THRESHOLD)
    def battery37 = zigbee.readAttribute(CLUSTER_POWER_CONFIGURATION, BATTERY_SETTING_VOLTAGE_THRESHOLD_1)
    def battery38 = zigbee.readAttribute(CLUSTER_POWER_CONFIGURATION, BATTERY_SETTING_VOLTAGE_THRESHOLD_2)
    def battery39 = zigbee.readAttribute(CLUSTER_POWER_CONFIGURATION, BATTERY_SETTING_VOLTAGE_THRESHOLD_3)
    def battery3a = zigbee.readAttribute(CLUSTER_POWER_CONFIGURATION, BATTERY_SETTING_PCT_MIN_THRESHOLD)
    def battery3b = zigbee.readAttribute(CLUSTER_POWER_CONFIGURATION, BATTERY_SETTING_PCT_THRESHOLD_1)
    def battery3c = zigbee.readAttribute(CLUSTER_POWER_CONFIGURATION, BATTERY_SETTING_PCT_THRESHOLD_2)
    def battery3d = zigbee.readAttribute(CLUSTER_POWER_CONFIGURATION, BATTERY_SETTING_PCT_THRESHOLD_3)
    def battery3e = zigbee.readAttribute(CLUSTER_POWER_CONFIGURATION, BATTERY_SETTING_ALARM_STATE)
    def strInfo =  "\nBattery 0x0020: " 	+ battery20 
    strInfo = strInfo + "\nBattery 0x0021: " + battery21	
    strInfo = strInfo + "\nBattery 0x0030: " + battery30	
    strInfo = strInfo + "\nBattery 0x0031: " + battery31	
    strInfo = strInfo + "\nBattery 0x0032: " + battery32	
    strInfo = strInfo + "\nBattery 0x0033: " + battery33	
    strInfo = strInfo + "\nBattery 0x0034: " + battery34	
    strInfo = strInfo + "\nBattery 0x0035: " + battery35	
    strInfo = strInfo + "\nBattery 0x0036: " + battery36	
    strInfo = strInfo + "\nBattery 0x0037: " + battery37	
    strInfo = strInfo + "\nBattery 0x0038: " + battery38	
    strInfo = strInfo + "\nBattery 0x0039: " + battery39
    strInfo = strInfo + "\nBattery 0x003a: " + battery3a	
    strInfo = strInfo + "\nBattery 0x003b: " + battery3b	
    strInfo = strInfo + "\nBattery 0x003c: " + battery3c	
    strInfo = strInfo + "\nBattery 0x003d: " + battery3d	
    strInfo = strInfo + "\nBattery 0x003e: " + battery3e	
    
    log.debug strInfo
    
    def strConf = zigbee.configureReporting(0x0001, 0x0021, UINT8, 600, 21600, 0x01)
    log.debug "Conf: " + strConf
    // [zdo bind 0x1D46 0x01 0x01 0x0001 {000D6F000CF4F4A5} {}, delay 2000, st cr 0x1D46 0x01 0x0001 0x0021 0x20 0x0258 0x5460 {01}, delay 2000]
    //return batteryInformation + batterySettings
    
    return battery20 + battery21 + battery30 + battery31 + battery32 + battery33 + battery34 + battery35 + battery36 + battery37 + battery38 + battery39 + battery3a + battery3b + battery3c + battery3d + battery3e
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
    return refresh()
}

def refresh() {
    zigbee.onOffRefresh() + zigbee.onOffConfig() + readBatteryState()
}

def configure() {
    // Device-Watch allows 2 check-in misses from device + ping (plus 2 min lag time)
    sendEvent(name: "checkInterval", value: 2 * 10 * 60 + 2 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
    log.debug "Configuring Reporting and Bindings."
    zigbee.onOffRefresh() + zigbee.onOffConfig()
}

// Private methods
private Map parseReportAttributeMessage(String description) {
    Map descMap = zigbee.parseDescriptionAsMap(description)
	Map resultMap = ["cluster": "unknown", "action": "momentary", "button": "unknown"]
    
    switch(descMap.clusterInt) {
    	case CLUSTER_SWITCH:
        	resultMap.cluster = "switch";
            
            switch (descMap.commandInt) {
            
            	case SWITCH_ON_CLICKED:
                	resultMap.button = "on";
                	break;
                    
                case SWITCH_OFF_CLICKED:
                	resultMap.button = "off";
                	break;
            }
        	break;
            
        case CLUSTER_SWITCH_LEVEL:
        	resultMap.cluster = "switch level";
            
            switch (descMap.commandInt) {
            
            	case SWITCH_LEVEL_CLICKED:
                	resultMap.action = "momentary";
                	break;
                    
                case SWITCH_LEVEL_DOWN:
                	resultMap.action = "pressed";
                    break;
                    
                case SWITCH_LEVEL_UP:
                	resultMap.action = "released";
                	break;
            }
            
            switch (descMap.data[0]) {
            
            	case SWITCH_LEVEL_BRIGHTER:
                	resultMap.button = "brighter";
                	break;
                    
                case SWITCH_LEVEL_DIMMER:
                	resultMap.button = "dimmer";
                    break;
            }
        	break;
    }
    
    return resultMap;
}