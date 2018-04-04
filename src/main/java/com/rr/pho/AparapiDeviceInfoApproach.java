package com.rr.pho;

import com.aparapi.device.Device;
import com.aparapi.device.OpenCLDevice;
import com.aparapi.internal.kernel.KernelManager;
import com.aparapi.internal.kernel.KernelPreferences;
import com.aparapi.internal.opencl.OpenCLPlatform;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AparapiDeviceInfoApproach {
    private static final Logger LOGGER = Logger.getLogger(AparapiDeviceInfoApproach.class);

    private void printPlatformsAndDevicesData() {
        List<OpenCLPlatform> platforms = (new OpenCLPlatform()).getOpenCLPlatforms();
        LOGGER.info("Machine contains " + platforms.size() + " OpenCL platforms");
        for (OpenCLPlatform platform : platforms) {
            LOGGER.debug("");
            LOGGER.info("Platform name: " + platform.getName());
            LOGGER.info("Vendor: \"" + platform.getVendor() + "\"");
            LOGGER.info("Version: \"" + platform.getVersion() + "\"");
            List<OpenCLDevice> devices = platform.getOpenCLDevices();
            LOGGER.debug("");
            LOGGER.info("Platform " + platform.getName() + " contains " + devices.size() + " OpenCL devices");
            for (OpenCLDevice device : devices) {
                if (device.getType() != Device.TYPE.GPU)
                    continue;
                LOGGER.info("Type: " + device.getType());
                LOGGER.info("Short description: " + device.getShortDescription());
                LOGGER.info("Device name: " + device.getName());
                LOGGER.info("MaxWorkItemDimensions: " + device.getMaxWorkItemDimensions());
                LOGGER.info("MaxWorkItemSizes" + Arrays.stream(device.getMaxWorkItemSize()).boxed().collect
                        (Collectors.toList()));
                LOGGER.info("MaxWorkGroupSize: " + device.getMaxWorkGroupSize());
            }
        }
    }

    public String getDeviceMaxWorkItemSizes() {
        printPlatformsAndDevicesData();
        List<Integer> maxWorkItemSizesList = new ArrayList<>();

        KernelPreferences preferences = KernelManager.instance().getDefaultPreferences();

        for (Device device : preferences.getPreferredDevices(null)) {
            if (device.getType() != Device.TYPE.GPU)
                continue;

//            you can get some device data even without method printPlatformsAndDevicesData, just uncomment the line below
//            LOGGER.info(device);
//            LOGGER.debug("");

            maxWorkItemSizesList.addAll(Arrays.stream(device.getMaxWorkItemSize()).boxed().collect(Collectors.toList
                    ()));
        }

        return "" + maxWorkItemSizesList.stream().max(Long::compare).orElse(-102); // just because I like -102
    }
}