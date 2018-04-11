#include <stdio.h>
#include <string.h>

#include "sqlite3.h"

#include "list.h"
#include "hashtable.h"

#include "game/catalogue/catalogue_manager.h"
#include "game/catalogue/catalogue_page.h"
#include "game/catalogue/catalogue_item.h"

#include "database/queries/catalogue_query.h"
#include "database/db_connection.h"

/**
 *
 */
void catalogue_query_pages() {
    sqlite3 *conn = db_create_connection();
    sqlite3_stmt *stmt;

    int status = sqlite3_prepare(conn, "SELECT id, min_role, name_index, name, layout, image_headline, image_teasers, body, label_pick, label_extra_s, label_extra_t FROM catalogue_pages", -1, &stmt, 0);

    if (status == SQLITE_OK) {
        // No binding needed here, sir!
    } else {
        fprintf(stderr, "Failed to execute statement: %s\n", sqlite3_errmsg(conn));
    }

    while (true) {
        status = sqlite3_step(stmt);

        if (status != SQLITE_ROW) {
            break;
        }

        catalogue_page *page = catalogue_page_create(
            sqlite3_column_int(stmt, 0),
            sqlite3_column_int(stmt, 1),
            strdup((char*)sqlite3_column_text(stmt, 2)),
            strdup((char*)sqlite3_column_text(stmt, 3)),
            strdup((char*)sqlite3_column_text(stmt, 4)),
            strdup((char*)sqlite3_column_text(stmt, 5)),
            strdup((char*)sqlite3_column_text(stmt, 6)),
            strdup((char*)sqlite3_column_text(stmt, 7)),
            sqlite3_column_text(stmt, 8) != NULL ? strdup((char*)sqlite3_column_text(stmt, 8)) : NULL,
            sqlite3_column_text(stmt, 9) != NULL ? strdup((char*)sqlite3_column_text(stmt, 9)) : NULL,
            sqlite3_column_text(stmt, 10) != NULL ? strdup((char*)sqlite3_column_text(stmt, 10)) : NULL
        );

        catalogue_manager_add_page(page);
    }

    sqlite3_finalize(stmt);
    sqlite3_close(conn);
}

/**
 *
 */
void catalogue_query_items() {
    sqlite3 *conn = db_create_connection();
    sqlite3_stmt *stmt;

    int status = sqlite3_prepare(conn, "SELECT * FROM catalogue_items", -1, &stmt, 0);

    if (status == SQLITE_OK) {
        // No binding needed here, sir!
    } else {
        fprintf(stderr, "Failed to execute statement: %s\n", sqlite3_errmsg(conn));
    }

    while (true) {
        status = sqlite3_step(stmt);

        if (status != SQLITE_ROW) {
            break;
        }

        catalogue_item *item = catalogue_item_create(
                (char *) sqlite3_column_text(stmt, 0),
                sqlite3_column_int(stmt, 1),
                sqlite3_column_int(stmt, 2),
                sqlite3_column_int(stmt, 3),
                sqlite3_column_int(stmt, 4),
                sqlite3_column_int(stmt, 5),
                (char *) sqlite3_column_text(stmt, 6),
                (char *) sqlite3_column_text(stmt, 7),
                (bool)sqlite3_column_int(stmt, 8)
        );

        catalogue_manager_add_item(item);
    }

    sqlite3_finalize(stmt);
    sqlite3_close(conn);
}