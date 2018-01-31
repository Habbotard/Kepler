#include "communication/messages/incoming_message.h"
#include "communication/messages/outgoing_message.h"

void GETFLATCAT(player *player, incoming_message *message) {
    int room_id = im_read_vl64(message);
	room *room = room_query_get_by_room_id(room_id);

	outgoing_message *om = om_create(222); // "C^"
	
	if (room != NULL) {
		om_write_int(om, room->room_id);	
		om_write_int(om, room->room_data->category);		
	}

	player_send(player, om);
	om_cleanup(om);
}