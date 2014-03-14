/**
 * ShakeDetector.java
 *
 * @author chengsiyu
 * 
 * @date 2011-3-28
 * 
 * Copyright 2011 netease. All rights reserved. 
 */
package com.hellodev.lightme.util;

import java.util.ArrayList;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * @author chengsiyu
 *
 */
public class ShakeDetector implements SensorEventListener
{
    private Context context;
    private SensorManager sensorManager;
    private ArrayList<OnShakeListener> listeners;
    private Long lastTime = null;
    private float lastX, lastY, lastZ;
    private long lastTrigger = 0;
    
    private static final int TIME_SHRESHOLD = 100;  
   
    private static final double SHAKE_SHRESHOLD = 100;
    
    
    public ShakeDetector(Context context)
    {
        this.context = context;
        sensorManager = (SensorManager)this.context.getSystemService(Context.SENSOR_SERVICE);
        listeners = new ArrayList<OnShakeListener>();
        lastTime = null;
    }

    public void start()
    {
        if(sensorManager != null)
        {
            Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if(sensor != null)
            {
                sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
            }
        }
    }
    
    public void stop()
    {
        if(sensorManager != null)
        {
            sensorManager.unregisterListener(this);
        }
    }

    public void registerOnShakeListener(OnShakeListener listener)
    {
        if(listener == null || listeners.contains(listener))
        {
            return;
        }
        listeners.add(listener);
    }
    
    public void removeOnShakeListener(OnShakeListener listener)
    {
        if(listener == null)
        {
            return;
        }
        listeners.remove(listener);
    }
    
    /* (non-Javadoc)
     * @see android.hardware.SensorEventListener#onAccuracyChanged(android.hardware.Sensor, int)
     */
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
    }

    /* (non-Javadoc)
     * @see android.hardware.SensorEventListener#onSensorChanged(android.hardware.SensorEvent)
     */
    public void onSensorChanged(SensorEvent event)
    {
        if(lastTime == null)
        {
            lastTime = System.currentTimeMillis();
            return; 
        }
        Long currentTime = System.currentTimeMillis();
        long diffTime = currentTime - lastTime;  
        if (diffTime < TIME_SHRESHOLD)
        {
            return;  
        }
        lastTime = currentTime;  
        float x = event.values[0];  
        float y = event.values[1];  
        float z = event.values[2];  
        float deltaX = x - lastX;  
        float deltaY = y - lastY;  
        float deltaZ = z - lastZ;  
        lastX = x;  
        lastY = y;  
        lastZ = z;  
        double delta = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ* deltaZ) * 1000/ diffTime;  
        if (delta > SHAKE_SHRESHOLD) 
        {  
            this.notifyListeners();  
        }  
    }

    private void notifyListeners() 
    {  
        long curTime = System.currentTimeMillis(); 
        if(curTime - lastTrigger < 1000)  // less than 1 second
        {
            return;
        }
        lastTrigger = curTime;
        for (OnShakeListener listener : listeners) 
        {  
            listener.onShake();  
        }  
    } 
    
    public interface OnShakeListener
    {
        public void onShake();
    }
}
