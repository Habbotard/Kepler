#include "communication/messages/incoming_message.h"
#include "communication/messages/outgoing_message.h"

#include "game/player/player.h"

void GETAVAILABLEBADGES(session *player, incoming_message *im) {
    player_refresh_badges(player);
}
