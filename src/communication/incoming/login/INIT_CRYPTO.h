#include "communication/messages/incoming_message.h"
#include "communication/messages/outgoing_message.h"

#include "game/player/player.h"

void INIT_CRYPTO(player *player, incoming_message *message) {
    outgoing_message *init_crypto = om_create(277); // 'DU"
    //om_write_int(init_crypto, 1);
    om_write_int(init_crypto, 0); // force crypto
    player_send(player, init_crypto);
    om_cleanup(init_crypto);
}