package com.kAIS.KAIMyEntity.renderer;

import com.kAIS.KAIMyEntity.NativeFunc;
import com.kAIS.KAIMyEntity.config.KAIMyEntityConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.*;

public class MMDModelManager {
    static Map<Entity, Model> models;
    static Map<String, Stack<IMMDModel>> modelPool;
    static long prevTime;

    public static void Init() {
        models = new HashMap<>();
        modelPool = new HashMap<>();
        prevTime = System.currentTimeMillis();
    }

    public static IMMDModel LoadModel(String modelName, long layerCount) {
        //Model path
        File modelDir = new File(MinecraftClient.getInstance().runDirectory, "KAIMyEntity/" + modelName);
        String modelDirStr = modelDir.getAbsolutePath();

        String modelFilenameStr;
        boolean isPMD;
        File pmxModelFilename = new File(modelDir, "model.pmx");
        if (pmxModelFilename.isFile()) {
            modelFilenameStr = pmxModelFilename.getAbsolutePath();
            isPMD = false;
        } else {
            File pmdModelFilename = new File(modelDir, "model.pmd");
            if (pmdModelFilename.isFile()) {
                modelFilenameStr = pmdModelFilename.getAbsolutePath();
                isPMD = true;
            } else {
                return null;
            }
        }

        return MMDModelOpenGL.Create(modelFilenameStr, modelDirStr, isPMD, layerCount);
    }

    public static MMDModelManager.Model GetModelOrInPool(Entity entity, String modelName, boolean isPlayer) {
        Model model = MMDModelManager.GetModel(entity);
        //Check if model is active.
        if (model == null) {
            //First check if modelPool has model.
            IMMDModel m = GetModelFromPool(modelName);
            if (m != null) {
                AddModel(entity, m, modelName, isPlayer);
                model = GetModel(entity);
                return model;
            }

            //Load model from file.
            m = LoadModel(modelName, isPlayer ? 3 : 1);
            if (m == null)
                return null;

            //Register Animation user because it's a new model
            MMDAnimManager.AddModel(m);

            AddModel(entity, m, modelName, isPlayer);
            model = GetModel(entity);
        }
        return model;

    }

    public static Model GetModel(Entity entity) {
        return models.get(entity);
    }

    public static IMMDModel GetModelFromPool(String modelName) {
        Stack<IMMDModel> pool = modelPool.get(modelName);
        if (pool == null)
            return null;
        if (pool.empty())
            return null;
        else
            return pool.pop();
    }

    public static void AddModel(Entity entity, IMMDModel model, String modelName, boolean isPlayer) {
        if (isPlayer) {
            NativeFunc nf = NativeFunc.GetInst();
            PlayerData pd = new PlayerData();
            pd.stateLayers = new PlayerData.EntityState[3];
            pd.playCustomAnim = false;
            pd.rightHandMat = nf.CreateMat();
            pd.leftHandMat = nf.CreateMat();
            pd.matBuffer = ByteBuffer.allocateDirect(64); //float * 16

            ModelWithPlayerData m = new ModelWithPlayerData();
            m.entity = entity;
            m.model = model;
            m.modelName = modelName;
            m.unusedTime = 0;
            m.playerData = pd;
            model.ResetPhysics();
            model.ChangeAnim(MMDAnimManager.GetAnimModel(model, "idle"), 0);
            models.put(entity, m);
        } else {
            ModelWithEntityState m = new ModelWithEntityState();
            m.entity = entity;
            m.model = model;
            m.modelName = modelName;
            m.unusedTime = 0;
            m.state = MMDModelManager.EntityState.Idle;
            model.ResetPhysics();
            model.ChangeAnim(MMDAnimManager.GetAnimModel(model, "idle"), 0);
            models.put(entity, m);
        }
    }

    public static void Update() {
        long deltaTime = System.currentTimeMillis() - prevTime;
        prevTime = System.currentTimeMillis();

        List<Entity> waitForDelete = new LinkedList<>();
        for (Model i : models.values()) {
            i.unusedTime += deltaTime;
            if (i.unusedTime > 10000) {
                TryModelToPool(i);
                waitForDelete.add(i.entity);
            }
        }

        for (Entity i : waitForDelete)
            models.remove(i);
    }

    public static void ReloadModel() {
        for (Model i : models.values())
            DeleteModel(i);
        models = new HashMap<>();
        for (Stack<IMMDModel> i : modelPool.values()) {
            for (IMMDModel j : i) {
                MMDModelOpenGL.Delete((MMDModelOpenGL) j);

                //Unregister animation user
                MMDAnimManager.DeleteModel(j);
            }
        }
        modelPool = new HashMap<>();
    }

    static void DeleteModel(Model model) {
        MMDModelOpenGL.Delete((MMDModelOpenGL) model.model);

        //Unregister animation user
        MMDAnimManager.DeleteModel(model.model);
    }

    static void TryModelToPool(Model model) {
        if (modelPool.size() > KAIMyEntityConfig.modelPoolMaxCount) {
            DeleteModel(model);
        } else {
            Stack<IMMDModel> pool = modelPool.computeIfAbsent(model.modelName, k -> new Stack<>());
            pool.push(model.model);
        }
    }

    enum EntityState {Idle, Walk, Swim, Ridden}

    static class Model {
        Entity entity;
        IMMDModel model;
        String modelName;
        long unusedTime;
    }

    static class ModelWithEntityState extends Model {
        EntityState state;
    }

    static class ModelWithPlayerData extends Model {
        PlayerData playerData;
    }

    static class PlayerData {
        boolean playCustomAnim; //Custom animation played in layer 0.
        long rightHandMat, leftHandMat;
        ByteBuffer matBuffer;
        static HashMap<EntityState, String> stateProperty = new HashMap<>() {{
            put(EntityState.Idle, "idle");
            put(EntityState.Walk, "walk");
            put(EntityState.Sprint, "sprint");
            put(EntityState.Air, "air");
            put(EntityState.OnLadder, "onLadder");
            put(EntityState.Swim, "swim");
            put(EntityState.Ride, "ride");
            put(EntityState.Sleep, "sleep");
            put(EntityState.ElytraFly, "elytraFly");
            put(EntityState.Die, "die");
            put(EntityState.SwingRight, "swingRight");
            put(EntityState.SwingLeft, "swingLeft");
            put(EntityState.Sneak, "sneak");
        }};
        EntityState[] stateLayers;

        enum EntityState {Idle, Walk, Sprint, Air, OnLadder, Swim, Ride, Sleep, ElytraFly, Die, SwingRight, SwingLeft, ItemRight, ItemLeft, Sneak}
    }
}
