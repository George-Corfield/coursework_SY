package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.*;
import javax.annotation.Nonnull;

import org.checkerframework.checker.units.qual.A;
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

	private final class TicketBoard implements Board.TicketBoard{

		private Player player;

		private TicketBoard(Player player){
			this.player = player;
		}
		public int getCount(Ticket ticket){
			return this.player.tickets().get(ticket);
		}
	}

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
			validSetup(setup);
			validMrX(mrX);
			validDetectives(ImmutableList.copyOf(detectives));
			this.setup = setup;
			this.remaining = remaining;
			this.log = log;
			this.mrX = mrX;
			this.detectives = detectives;
		}
		public void validMrX(Player mrX){
			if(mrX == null) throw new NullPointerException();
			if(!mrX.isMrX()) throw new IllegalArgumentException();
		}
		public void validDetectives(ImmutableList<Player> detectives){
			if(detectives == null) throw new NullPointerException();
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
		}
		public void validSetup(GameSetup setup){
			if (setup.moves.isEmpty()) throw new IllegalArgumentException();
			if (setup.graph.nodes().isEmpty()) throw new IllegalArgumentException();
		}
		@Override
		public GameSetup getSetup(){
			return this.setup;
		}
		@Override
		public ImmutableSet<Piece> getPlayers(){
			List<Piece> players = new ArrayList<>();
			players.add(this.mrX.piece());
			for (int i = 0; i < this.detectives.size(); i++){
				players.add(this.detectives.get(i).piece());
			}
			return ImmutableSet.copyOf(players);

		}
		@Override
		public GameState advance(Move move){
			return null;
		}
		@Override
		public Optional<Integer> getDetectiveLocation(Detective detective){
			for(int i=0; i < this.detectives.size();i++){
				if (this.detectives.get(i).piece() == detective){
					return Optional.of(this.detectives.get(i).location());
				}
			}
			return Optional.empty();
		}
		@Override
		public Optional<TicketBoard> getPlayerTickets(Piece piece){
			if (this.getPlayers().contains(piece)){
				if (piece == this.mrX.piece()) {
					return Optional.of(new MyGameStateFactory.TicketBoard(this.mrX)); // what is ticketBoard
				}
				else {
					for (int i = 0; i < this.detectives.size(); i++) {
						if (piece == this.detectives.get(i).piece()) {
							return Optional.of(new MyGameStateFactory.TicketBoard(this.detectives.get(i)));
						}
					}
					return Optional.empty();
				}
			}
			else return Optional.empty();
		}
		@Override
		public ImmutableList<LogEntry> getMrXTravelLog(){
			return this.log;
		}
		@Override
		public ImmutableSet<Piece> getWinner(){
			this.winner = ImmutableSet.of();
			return this.winner;
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
		//TODO set up tests for setup variable then implement GameState once passed tests
		return new MyGameState(setup,ImmutableSet.of(MrX.MRX),ImmutableList.of(),mrX,detectives );
	}

}
