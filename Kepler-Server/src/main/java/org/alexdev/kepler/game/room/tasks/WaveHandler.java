package org.alexdev.kepler.game.room.tasks;

import org.alexdev.kepler.game.entity.Entity;
import org.alexdev.kepler.game.room.enums.StatusType;
import org.alexdev.kepler.messages.outgoing.rooms.user.USER_OBJECTS;
import org.alexdev.kepler.messages.outgoing.rooms.user.USER_STATUSES;

import java.util.List;

public class WaveHandler implements Runnable {
    private final Entity entity;

    public WaveHandler(Entity entity) {
        this.entity = entity;
    }

    @Override
    public void run() {
        if (this.entity.getRoom() == null) {
            return;
        }

        this.entity.getRoomUser().removeStatus(StatusType.WAVE);
        this.entity.getRoomUser().removeStatus(StatusType.SLEEP);

        if (!this.entity.getRoomUser().isWalking()) {
            this.entity.getRoom().send(new USER_STATUSES(List.of(this.entity)));
        }
    }
}
