#include "util/stringbuilder.h"
#include "lib/dyad/dyad.h"

#include "communication/messages/incoming_message.h"
#include "communication/messages/outgoing_message.h"

void message_login(player *player, incoming_message *message) {
    char *username = im_read_str(message);
    char *password = im_read_str(message);

    printf("Username: %s and password: %s\n", username, password);

    outgoing_message *om;

    om = om_create(2);
    player_send(player, om);

    om = om_create(3);
    player_send(player, om);

    free(username);
    free(password);


}
