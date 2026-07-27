package com.app.rush47.adapters;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.rush47.R;
import com.app.rush47.models.Tournament;

import java.util.List;
import java.util.Locale;

/**
 * Feeds match cards on the Matches list and My Matches screens. The
 * card's bottom button changes meaning depending on where it's shown:
 * on the browse list it's a real "Join" action; on My Matches (where
 * you're already joined) it's disabled and just says "JOINED" - pass
 * showJoinAction=false there so taps don't try to join again.
 */
public class TournamentAdapter extends RecyclerView.Adapter<TournamentAdapter.TournamentViewHolder> {

    public interface OnTournamentClickListener {
        void onCardClick(Tournament tournament);

        void onJoinClick(Tournament tournament);
    }

    private final List<Tournament> tournaments;
    private final OnTournamentClickListener listener;
    private final boolean showJoinAction;

    public TournamentAdapter(List<Tournament> tournaments, boolean showJoinAction, OnTournamentClickListener listener) {
        this.tournaments = tournaments;
        this.showJoinAction = showJoinAction;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TournamentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tournament_card, parent, false);
        return new TournamentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TournamentViewHolder holder, int position) {
        Tournament t = tournaments.get(position);
        android.content.Context context = holder.itemView.getContext();

        holder.title.setText(t.getTitle());

        String mapText = TextUtils.isEmpty(t.getMapName()) ? "" : t.getMapName() + "  |  ";
        holder.mapTime.setText(mapText + formatMatchTime(t.getMatchTime()));

        holder.entryFee.setText(t.getEntryFee());
        holder.prize.setText(t.getPrize());
        holder.perKill.setText(t.getPerKill());

        holder.slotsProgress.setMax(Math.max(t.getSlotTotal(), 1));
        holder.slotsProgress.setProgress(t.getSlotsFilled());
        holder.slotsText.setText(context.getString(R.string.slots_filled_format, t.getSlotsFilled(), t.getSlotTotal()));

        bindStatusChip(holder, t, context);
        bindJoinButton(holder, t, context);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onCardClick(t);
        });
    }

    private void bindStatusChip(TournamentViewHolder holder, Tournament t, android.content.Context context) {
        holder.statusChip.setVisibility(View.VISIBLE);
        int color;
        String label;
        switch (t.getStatus()) {
            case "ongoing":
                color = context.getResources().getColor(R.color.newgreen);
                label = "LIVE";
                break;
            case "completed":
                color = context.getResources().getColor(R.color.cb_dark_grey);
                label = "COMPLETED";
                break;
            default:
                color = context.getResources().getColor(R.color.purple);
                label = "UPCOMING";
        }
        holder.statusChip.setText(label);
        holder.statusChip.getBackground().mutate().setTint(color);
    }

    private void bindJoinButton(TournamentViewHolder holder, Tournament t, android.content.Context context) {
        if (!showJoinAction) {
            holder.joinButton.setText(R.string.joined);
            holder.joinButton.setAlpha(0.6f);
            holder.joinButton.setOnClickListener(null);
            return;
        }

        holder.joinButton.setAlpha(1f);
        if (t.isJoined()) {
            holder.joinButton.setText(R.string.joined);
            holder.joinButton.setAlpha(0.6f);
            holder.joinButton.setOnClickListener(null);
        } else if (t.isFull() || "completed".equals(t.getStatus())) {
            holder.joinButton.setText(t.isFull() ? R.string.match_full : R.string.completed);
            holder.joinButton.setAlpha(0.6f);
            holder.joinButton.setOnClickListener(null);
        } else {
            holder.joinButton.setText(R.string.join);
            holder.joinButton.setOnClickListener(v -> {
                if (listener != null) listener.onJoinClick(t);
            });
        }
    }

    /** "2026-07-27 20:30:00" -> "27 Jul, 08:30 PM" (falls back to the raw string on parse failure). */
    private String formatMatchTime(String raw) {
        if (TextUtils.isEmpty(raw)) return "";
        try {
            java.text.SimpleDateFormat in = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            java.text.SimpleDateFormat out = new java.text.SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault());
            return out.format(in.parse(raw));
        } catch (Exception e) {
            return raw;
        }
    }

    @Override
    public int getItemCount() {
        return tournaments.size();
    }

    static class TournamentViewHolder extends RecyclerView.ViewHolder {
        TextView title, statusChip, mapTime, entryFee, prize, perKill, slotsText, joinButton;
        ProgressBar slotsProgress;

        TournamentViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.matchTitle);
            statusChip = itemView.findViewById(R.id.matchStatusChip);
            mapTime = itemView.findViewById(R.id.matchMapTime);
            entryFee = itemView.findViewById(R.id.matchEntryFee);
            prize = itemView.findViewById(R.id.matchPrize);
            perKill = itemView.findViewById(R.id.matchPerKill);
            slotsProgress = itemView.findViewById(R.id.matchSlotsProgress);
            slotsText = itemView.findViewById(R.id.matchSlotsText);
            joinButton = itemView.findViewById(R.id.matchJoinButton);
        }
    }
}
