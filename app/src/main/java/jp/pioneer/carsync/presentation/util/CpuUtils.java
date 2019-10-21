package jp.pioneer.carsync.presentation.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yangming on 18-9-11.
 */
public class CpuUtils {

    /**
     * Get the list of all supported frequencies
     * <p>
     * example :
     * $ cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies
     * 300000 364800 441600 518400 595200 672000 748800 825600 883200 960000 1036800 1094400 1171200 1248000 1324800 1401600 1478400 1555200 1670400 1747200 1824000 1900800
     *
     * @param core CPU serial number, for example, cpu0 's serial number is 0
     * @return List of all supported frequencies
     */
    public static String[] readAvailableFrequencies(int core) {
        File scalingAvailableFrequenciesFile = new File(
                "/sys/devices/system/cpu/cpu" + core + "/cpufreq/scaling_available_frequencies");
        ArrayList<String> availableFrequencies = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(scalingAvailableFrequenciesFile);
            while (scanner.hasNext()) {
                availableFrequencies.add(scanner.next());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String[] availableFrequenciesArray = new String[availableFrequencies.size()];
        availableFrequenciesArray = availableFrequencies.toArray(availableFrequenciesArray);
        return availableFrequenciesArray;
    }

    /**
     * This shows the maximum operating frequency the processor can run at (in kHz)
     *
     * @param core CPU serial number
     * @return Maximum operating frequency
     */
    public static String readMaxCPUFrequency(int core) {
        return readFile("/sys/devices/system/cpu/cpu" + core + "/cpufreq/cpuinfo_max_freq");
    }

    /**
     * This shows the minimum operating frequency the processor can run at (in kHz)
     *
     * @param core CPU serial number
     * @return Minimum operating frequency
     */
    public static String readMinCPUFrequency(int core) {
        return readFile("/sys/devices/system/cpu/cpu" + core + "/cpufreq/cpuinfo_min_freq");
    }

    /**
     * Current frequency of the CPU as determined by the governor and cpufreq core,
     * in KHz. This is the frequency the kernel thinks the CPU runs at.
     *
     * @param core cpu serial number
     * @return Current frequency of the CPU
     */
    public static String readCurrentCPUFrequency(int core) {
        return readFile("/sys/devices/system/cpu/cpu" + core + "/cpufreq/scaling_cur_freq");
    }

    private static String readFile(String path) {
        String result = null;
        File file = new File(path);
        if (file.exists()) {
            InputStream inputStream;
            try {
                inputStream = new FileInputStream(file);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, length);
                }
                result = byteArrayOutputStream.toString("UTF-8");
                inputStream.close();
                byteArrayOutputStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * Get the number of CPU cores
     *
     * @return Number of CPU cores
     */
    public static int getCPUCoreNumber() {
        int number = 0;
        File file = new File("/sys/devices/system/cpu");
        String[] strings = file.list();
        StringBuilder builder = new StringBuilder();
        for (String string : strings) {
            builder.append(string);
        }
        Pattern pattern = Pattern.compile("cpu[0-9]{1}");
        Matcher matcher = pattern.matcher(builder.toString());
        while (matcher.find()) {
            number++;
        }
        return number;
    }
}
