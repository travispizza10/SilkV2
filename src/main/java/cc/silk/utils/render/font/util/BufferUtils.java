package cc.silk.utils.render.font.util;

import com.google.common.base.Preconditions;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;

public class BufferUtils {
    /**
     * Draws a buffer
     *
     * @param builder The buffer
     */
    public static void draw(BufferBuilder builder) {
        BufferRenderer.drawWithGlobalProgram(builder.end());
    }

    

    /**
     * Uploads this buffer to a VBO
     *
     * @param builder The buffer to upload
     * @param buffer  The VBO to upload to
     */
    public static void uploadToVbo(BuiltBuffer builder, VertexBuffer buffer) {
        Preconditions.checkArgument(!buffer.isClosed(), "VBO is closed");
        buffer.bind();
        buffer.upload(builder);
        VertexBuffer.unbind();
    }
}
