package com.app.rush47.models;

import org.json.JSONObject;

/**
 * One match (tournament or solo contest). Populated from
 * tournaments.php / tournament_detail.php / my_matches.php - not
 * every field is present from every endpoint (e.g. the plain list
 * doesn't send room_id/room_password), so callers should treat
 * missing fields as empty/false rather than assume every field is set.
 */
public class Tournament {

    private final String tournamentId;
    private final String gameId;
    private final String type;
    private final String title;
    private final String entryFee;
    private final String prize;
    private final String perKill;
    private final String mapName;
    private final String matchTime;
    private final int slotTotal;
    private final int slotsFilled;
    private final String slotNumber;
    private final String status;
    private final boolean joined;
    private final String roomId;
    private final String roomPassword;

    public Tournament(String tournamentId, String gameId, String type, String title,
                       String entryFee, String prize, String perKill, String mapName,
                       String matchTime, int slotTotal, int slotsFilled, String slotNumber,
                       String status, boolean joined, String roomId, String roomPassword) {
        this.tournamentId = tournamentId;
        this.gameId = gameId;
        this.type = type;
        this.title = title;
        this.entryFee = entryFee;
        this.prize = prize;
        this.perKill = perKill;
        this.mapName = mapName;
        this.matchTime = matchTime;
        this.slotTotal = slotTotal;
        this.slotsFilled = slotsFilled;
        this.slotNumber = slotNumber;
        this.status = status;
        this.joined = joined;
        this.roomId = roomId;
        this.roomPassword = roomPassword;
    }

    public String getTournamentId() {
        return tournamentId;
    }

    public String getGameId() {
        return gameId;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getEntryFee() {
        return entryFee;
    }

    public String getPrize() {
        return prize;
    }

    public String getPerKill() {
        return perKill;
    }

    public String getMapName() {
        return mapName;
    }

    public String getMatchTime() {
        return matchTime;
    }

    public int getSlotTotal() {
        return slotTotal;
    }

    public int getSlotsFilled() {
        return slotsFilled;
    }

    public String getSlotNumber() {
        return slotNumber;
    }

    public String getStatus() {
        return status;
    }

    public boolean isJoined() {
        return joined;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getRoomPassword() {
        return roomPassword;
    }

    public boolean isFull() {
        return slotTotal > 0 && slotsFilled >= slotTotal;
    }

    /**
     * Builds a Tournament from any of tournaments.php / tournament_detail.php /
     * my_matches.php's per-match JSON object. Missing fields fall back to
     * safe defaults instead of throwing, since each endpoint only sends
     * the fields relevant to it.
     */
    public static Tournament fromJson(JSONObject o) {
        return new Tournament(
                o.optString("tournament_id", ""),
                o.optString("game_id", ""),
                o.optString("type", "tournament"),
                o.optString("title", ""),
                o.optString("entry_fee", "0"),
                o.optString("prize", "0"),
                o.optString("per_kill", "0"),
                o.optString("map_name", ""),
                o.optString("match_time", ""),
                o.optInt("slot_total", 0),
                o.optInt("slots_filled", 0),
                o.optString("slot_number", ""),
                o.optString("status", "upcoming"),
                o.optBoolean("is_joined", false),
                o.optString("room_id", ""),
                o.optString("room_password", ""));
    }
}
