package com.kAIS.KAIMyEntity.renderer;

import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileWriter;

public class RenderTimer
{
    public static void Init()
    {
        File log = new File(Minecraft.getMinecraft().gameDir, "KAIMyEntity/renderTimer.log");
        if (log.exists())
        {
            shouldUse = true;
            inst = new RenderTimer();
            try
            {
                log.delete();
                log.createNewFile();
                inst.ofs = new FileWriter(log);
            }
            catch (Exception e)
            {
                shouldUse = false;
            }
            inst.active = false;
            inst.prevRecordTime = System.currentTimeMillis();
        }
        else
        {
            shouldUse = false;
        }
    }

    public static boolean ShouldUse()
    {
        return shouldUse;
    }

    public static RenderTimer GetInst()
    {
        return inst;
    }

    public static void BeginIfUse(String recordName)
    {
        if (ShouldUse())
        {
            if (GetInst().deltaRecordTime > 500)
                GetInst().Begin(recordName);
        }
    }

    public static void EndIfUse()
    {
        if (ShouldUse())
        {
            if (GetInst().deltaRecordTime > 500)
                GetInst().End();
        }
    }

    public static void LogIfUse(String str)
    {
        if (ShouldUse())
        {
            if (GetInst().deltaRecordTime > 500)
                GetInst().Log(str);
        }
    }

    public static void BeginRecord()
    {
        if (ShouldUse())
        {
            GetInst().deltaRecordTime = System.currentTimeMillis() - GetInst().prevRecordTime;
        }
    }

    public static void EndRecord()
    {
        if (ShouldUse())
        {
            if (GetInst().deltaRecordTime > 500)
                GetInst().prevRecordTime = System.currentTimeMillis();
        }
    }

    public void Begin(String recordName)
    {
        if (active)
            return;
        active = true;
        this.recordName = recordName;
        prevTime = System.currentTimeMillis();
    }

    public void End()
    {
        if (!active)
            return;
        active = false;
        long deltaTime = System.currentTimeMillis() - prevTime;
        try
        {
            ofs.append(String.format("[KAIMyEntity Render Timer] \"%s\" cost time: %d\r\n", recordName, deltaTime));
            ofs.flush();
        }
        catch (Exception ignored)
        {

        }
    }

    public void Log(String str)
    {
        try
        {
            ofs.append(String.format("[KAIMyEntity Render Timer] %s\r\n", str));
            ofs.flush();
        }
        catch (Exception ignored)
        {

        }
    }

    RenderTimer()
    {

    }

    static boolean shouldUse;
    static RenderTimer inst;

    FileWriter ofs;
    boolean active;
    String recordName;
    long prevTime;
    long deltaRecordTime;
    long prevRecordTime;
}
