#include "util/stringbuilder.h"
#include "lib/dyad/dyad.h"

#include "communication/messages/incoming_message.h"
#include "communication/messages/outgoing_message.h"

#include "database/queries/player_query.h"
#include "shared.h"

void APPROVENAME(player *player, incoming_message *message) {
    char *username = im_read_str(message);
    int name_check_code = get_name_check_code(username);

    outgoing_message *om = om_create(36); // "@d"
    om_write_int(om, name_check_code);
    player_send(player, om);
    om_cleanup(om);
}