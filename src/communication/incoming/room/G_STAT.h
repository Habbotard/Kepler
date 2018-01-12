#include "util/stringbuilder.h"
#include "lib/dyad/dyad.h"

#include "communication/messages/incoming_message.h"
#include "communication/messages/outgoing_message.h"

void G_STAT(player *player, incoming_message *message) {
    outgoing_message *om = om_create(34); // "@b"
    player_send(player, om);
    om_cleanup(om);
}
