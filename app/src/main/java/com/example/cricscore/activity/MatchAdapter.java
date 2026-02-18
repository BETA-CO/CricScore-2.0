package com.example.cricscore.activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cricscore.R;
import com.example.cricscore.dataTypes.Match;

import java.util.List;

public class MatchAdapter extends RecyclerView.Adapter<MatchAdapter.MatchViewHolder> {

    public interface OnMatchClickListener {
        void onMatchClick(Match match);
        void onDeleteClick(Match match);
    }

    private List<Match> matches;
    private OnMatchClickListener listener;

    public MatchAdapter(List<Match> matches, OnMatchClickListener listener) {
        this.matches = matches;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_match, parent, false);
        return new MatchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MatchViewHolder holder, int position) {
        Match match = matches.get(position);
        holder.tvDate.setText(match.date);
        holder.tvTeams.setText(match.teamA + " vs " + match.teamB);
        holder.tvScores.setText(match.scoreA + "/" + match.wicketsA + " (" + match.oversA + ") vs " +
                               match.scoreB + "/" + match.wicketsB + " (" + match.oversB + ")");
        holder.tvWinner.setText("Winner: " + match.winner);

        holder.itemView.setOnClickListener(v -> listener.onMatchClick(match));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(match));
    }

    @Override
    public int getItemCount() {
        return matches.size();
    }

    static class MatchViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvTeams, tvScores, tvWinner;
        ImageButton btnDelete;

        public MatchViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTeams = itemView.findViewById(R.id.tvTeams);
            tvScores = itemView.findViewById(R.id.tvScores);
            tvWinner = itemView.findViewById(R.id.tvWinner);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
