package com.msemu.renx.nodes;

import com.msemu.renx.NXFile;
import com.msemu.renx.NXNodeType;
import javafx.scene.media.Media;

import java.util.Spliterator;
import java.util.function.Consumer;

public class NXAudioNode extends NXNode<Media> {

    public NXAudioNode(NXFile nxFile, long nodeDataOffset) {
        super(nxFile, NXNodeType.AUDIO, nodeDataOffset);
    }

    @Override
    public Media getValue() {
        //TODO parsing mp3
        return null;
    }

}
