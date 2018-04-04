package com.rr.pho;

import static org.jocl.CL.*;

import java.nio.*;
import java.util.*;

import org.apache.log4j.Logger;
import org.jocl.*;

public class JOCLDeviceInfoApproach {
    private static final Logger LOGGER = Logger.getLogger(JOCLDeviceInfoApproach.class);

    private long getLong(cl_device_id device, int paramName) {
        return getLongs(device, paramName, 1)[0];
    }

    private long[] getLongs(cl_device_id device, int paramName, int numValues) {
        long[] values = new long[numValues];
        clGetDeviceInfo(device, paramName, Sizeof.cl_long * (long)numValues, Pointer.to(values), null);
        return values;
    }

    private String getString(cl_platform_id platform, int paramName) {
        long[] size = new long[1];
        clGetPlatformInfo(platform, paramName, 0, null, size);

        byte[] buffer = new byte[(int) size[0]];
        clGetPlatformInfo(platform, paramName, buffer.length, Pointer.to(buffer), null);
        return new String(buffer, 0, buffer.length - 1);
    }

    private String getString(cl_device_id device, int paramName) {
        long[] size = new long[1];
        clGetDeviceInfo(device, paramName, 0, null, size);

        byte[] buffer = new byte[(int) size[0]];
        clGetDeviceInfo(device, paramName, buffer.length, Pointer.to(buffer), null);
        return new String(buffer, 0, buffer.length - 1);
    }

    private long getSize(cl_device_id device, int paramName) {
        return getSizes(device, paramName, 1)[0];
    }

    private Long[] getSizes(cl_device_id device, int paramName, int numValues) {
        ByteBuffer buffer = ByteBuffer.allocate(
                numValues * Sizeof.size_t).order(ByteOrder.nativeOrder());
        clGetDeviceInfo(device, paramName, Sizeof.size_t * (long)numValues,
                Pointer.to(buffer), null);
        Long[] values = new Long[numValues];
        for (int i = 0; i < numValues; i++) {
            values[i] = buffer.getLong(i * Sizeof.size_t);
        }
        return values;
    }

    private List<cl_device_id> getPlatformData() {
        int[] numPlatforms = new int[1];
        clGetPlatformIDs(0, null, numPlatforms);

        LOGGER.info("Machine contains " + numPlatforms[0] + " OpenCL platforms");

        cl_platform_id[] platforms = new cl_platform_id[numPlatforms[0]];
        clGetPlatformIDs(platforms.length, platforms, null);

        List<cl_device_id> devices = new ArrayList<>();
        for (cl_platform_id platform : platforms) {
            String platformName = getString(platform, CL_PLATFORM_NAME);

            int[] numDevices = new int[1];
            clGetDeviceIDs(platform, CL_DEVICE_TYPE_ALL, 0, null, numDevices);

            LOGGER.info("Number of devices in platform " + platformName + ": " + numDevices[0]);

            cl_device_id[] devicesArray = new cl_device_id[numDevices[0]];
            clGetDeviceIDs(platform, CL_DEVICE_TYPE_ALL, numDevices[0], devicesArray, null);

            devices.addAll(Arrays.asList(devicesArray));
        }
        return devices;
    }

    private List<Long> getGPUDevicesData(List<cl_device_id> devices, int maxWorkItemSizes) {
        List<Long> maxWorkItemSizesList = new ArrayList<>();
        for (cl_device_id device : devices) {
            long deviceType = getLong(device, CL_DEVICE_TYPE); // CPU - 2, GPU - 4
            if ((deviceType & CL_DEVICE_TYPE_GPU) == 0)
                continue;

            String deviceName = getString(device, CL_DEVICE_NAME);
            String deviceVendor = getString(device, CL_DEVICE_VENDOR);
            String driverVersion = getString(device, CL_DRIVER_VERSION);
            long maxWorkItemDimensions = getLong(device, CL_DEVICE_MAX_WORK_ITEM_DIMENSIONS);
            Long[] maxWorkItemSizesArray = getSizes(device, maxWorkItemSizes, 3);

            maxWorkItemSizesList.addAll(Arrays.asList(maxWorkItemSizesArray));
            long maxWorkGroupSize = getSize(device, CL_DEVICE_MAX_WORK_GROUP_SIZE);

            LOGGER.debug("");
            LOGGER.info("------ Info for device " + deviceName + ": ------");
            LOGGER.info("CL_DEVICE_TYPE: CL_DEVICE_TYPE_GPU");
            LOGGER.info("CL_DEVICE_NAME: " + deviceName);
            LOGGER.info("CL_DEVICE_VENDOR: " + deviceVendor);
            LOGGER.info("CL_DRIVER_VERSION: " + driverVersion);
            LOGGER.info("CL_DEVICE_MAX_WORK_ITEM_DIMENSIONS: " + maxWorkItemDimensions);
            LOGGER.info("CL_DEVICE_MAX_WORK_ITEM_SIZES: " + maxWorkItemSizesArray[0] + " / " +
                    maxWorkItemSizesArray[1] + " / " + maxWorkItemSizesArray[2]);
            LOGGER.info("CL_DEVICE_MAX_WORK_GROUP_SIZE: " + maxWorkGroupSize);
        }
        return maxWorkItemSizesList;
    }

    public String getDeviceMaxWorkItemSizes(int maxWorkItemSizes) {
        List<cl_device_id> platformData = getPlatformData();
        List<Long> maxWorkItemSizesList = getGPUDevicesData(platformData, maxWorkItemSizes);
        return "" + maxWorkItemSizesList.stream().max(Long::compare).orElse(-102L); // just because I like -102L
    }
}