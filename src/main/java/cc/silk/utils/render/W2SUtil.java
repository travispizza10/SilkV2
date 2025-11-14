package cc.silk.utils.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

public class W2SUtil {
    public static Matrix4f matrixProject = new Matrix4f();
    public static Matrix4f matrixModel = new Matrix4f();
    public static Matrix4f matrixWorldSpace = new Matrix4f();

    public static Vec3d getCoords(Vec3d vector) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.getEntityRenderDispatcher() == null || mc.getEntityRenderDispatcher().camera == null) {
            return null;
        }
        
        Camera camera = mc.getEntityRenderDispatcher().camera;
        Vec3d cameraPos = camera.getPos();
        
        return getCoords(vector, cameraPos);
    }
    
    public static Vec3d getCoords(Vec3d vector, Vec3d cameraPos) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) {
            return null;
        }
        
        int displayHeight = mc.getWindow().getFramebufferHeight();
        int[] viewport = new int[4];

        GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);

        Vector3f target = new Vector3f();

        double deltaX = vector.x - cameraPos.x;
        double deltaY = vector.y - cameraPos.y;
        double deltaZ = vector.z - cameraPos.z;

        Vector4f transformedCoordinates = new Vector4f((float) deltaX, (float) deltaY, (float) deltaZ, 1.0f).mul(matrixWorldSpace);

        Matrix4f matrixProj = new Matrix4f(matrixProject);
        Matrix4f matrixMod = new Matrix4f(matrixModel);

        matrixProj.mul(matrixMod).project(transformedCoordinates.x(), transformedCoordinates.y(), transformedCoordinates.z(), viewport, target);

        return new Vec3d(target.x / mc.getWindow().getScaleFactor(), (displayHeight - target.y) / mc.getWindow().getScaleFactor(), target.z);
    }
}
