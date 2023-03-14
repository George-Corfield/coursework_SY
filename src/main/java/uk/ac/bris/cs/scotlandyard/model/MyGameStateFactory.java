package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.*;
import javax.annotation.Nonnull;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.Move.*;
import uk.ac.bris.cs.scotlandyard.model.Piece.*;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;

import javax.annotation.Nonnull;

import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

/**
 * cw-model
 * Stage 1: Complete this class
 */
public final class MyGameStateFactory implements Factory<GameState> {

	private final class MyGameState implements GameState{
		private GameSetup setup;
		private ImmutableSet<Piece> remaining;
		private ImmutableList<LogEntry> log;
		private Player mrX;
		private List<Player> detectives;
		private ImmutableSet<Move> moves;
		private ImmutableSet<Piece> winner;

		private MyGameState(
				final GameSetup setup,
				final ImmutableSet<Piece> remaining,
				final ImmutableList<LogEntry> log,
				final Player mrX,
				final List<Player> detectives){

		}
		@Override
		public GameSetup getSetup(){
			return null;
		}
		@Override
		public ImmutableSet<Piece> getPlayers(){
			return null;
		}
		@Override
		public GameState advance(Move move){
			return null;
		}
		@Override
		public Optional<Integer> getDetectiveLocation(Detective detective){
			return null;
		}
		@Override
		public Optional<TicketBoard> getPlayerTickets(Piece piece){
			return null;
		}
		@Override
		public ImmutableList<LogEntry> getMrXTravelLog(){
			return null;
		}
		@Override
		public ImmutableSet<Piece> getWinner(){
			return null;
		}
		@Override
		public ImmutableSet<Move> getAvailableMoves(){
			return null;
		}
	}

	@Nonnull @Override public GameState build(
			GameSetup setup,
			Player mrX,
			ImmutableList<Player> detectives) {
		if(mrX == null || detectives == null) throw new NullPointerException();
		if(!mrX.isMrX()) throw new IllegalArgumentException();
		List<Player> validDetectives = new ArrayList<>();
		for (int i = 0; i < detectives.size(); i++){
			Player currentDetective = detectives.get(i);
			if (currentDetective == null) throw new NullPointerException();
			if (currentDetective.isMrX()) throw new IllegalArgumentException();
			if (currentDetective.has(Ticket.SECRET) || currentDetective.has(Ticket.DOUBLE)) throw new IllegalArgumentException();
			for (int j = 0; j < validDetectives.size(); j++){
				Player validDetective = validDetectives.get(j);
				if (currentDetective.piece().equals(validDetective.piece())) throw new IllegalArgumentException();
				if (currentDetective.location() == validDetective.location()) throw new IllegalArgumentException();
			}
			validDetectives.add(currentDetective);

		}
		if (setup.moves.isEmpty()) throw new IllegalArgumentException();
		if (setup.graph.nodes().isEmpty()) throw new IllegalArgumentException();
		//TODO set up tests for setup variable then implement GameState once passed tests
		return new MyGameState(setup,ImmutableSet.of(MrX.MRX),ImmutableList.of(),mrX,detectives );
	}

}
