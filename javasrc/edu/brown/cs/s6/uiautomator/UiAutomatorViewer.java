/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.brown.cs.s6.uiautomator;

import com.android.SdkConstants;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;

import edu.brown.cs.s6.uiautomator.UiAutomatorHelper.UiAutomatorException;
import edu.brown.cs.s6.uiautomator.UiAutomatorTree.UiNode;

import java.io.File;

public class UiAutomatorViewer{
    
    private static String getAdbLocation() {
        String toolsDir = System.getProperty("user.dir"); //$NON-NLS-1$
        
        if (toolsDir == null) {
            return null;
        }
        
        File sdk = new File(toolsDir).getParentFile();
        
        // check if adb is present in platform-tools
        File platformTools = new File(sdk, "platform-tools");
        File adb = new File(platformTools, SdkConstants.FN_ADB);
        if (adb.exists()) {
            return adb.getAbsolutePath();
        }
        
        // check if adb is present in the tools directory
        adb = new File(toolsDir, SdkConstants.FN_ADB);
        if (adb.exists()) {
            return adb.getAbsolutePath();
        }
        
        // check if we're in the Android source tree where adb is in $ANDROID_HOST_OUT/bin/adb
        String androidOut = System.getenv("ANDROID_HOST_OUT");
        if (androidOut != null) {
            String adbLocation = androidOut + File.separator + "bin" + File.separator +
            SdkConstants.FN_ADB;
            if (new File(adbLocation).exists()) {
                return adbLocation;
            }
        }
        
        return null;
    }
    
    public UiNode getHierarchy() throws UiAutomatorException{
        
        AndroidDebugBridge.init(false);
        
        AndroidDebugBridge debugBridge = AndroidDebugBridge.createBridge(getAdbLocation(), true);
        if (debugBridge == null) {
            System.err.println("Invalid ADB location.");
            System.exit(1);
        }
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        IDevice device = null;
        final IDevice[] devices = debugBridge.getDevices();
        if(devices == null || devices.length == 0)	{
            System.err.println("No device found.");
            System.exit(1);
        } else	device = devices[0];
        
        UiAutomatorHelper.takeSnapshot(device);
        
        UiNode xmlRootNode = UiAutomatorHelper.getXMLRootNode();
        xmlRootNode.setScreenWidth(xmlRootNode.width);
        xmlRootNode.setScreenHeight(xmlRootNode.height);
        
        return xmlRootNode;
    }
    
}
