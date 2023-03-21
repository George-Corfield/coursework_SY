package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
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
	//
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
			this.moves = getAvailableMoves();
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
			List<Piece> pieces = new ArrayList<>();
			pieces.add(this.mrX.piece());
			for (Player detective : this.detectives){
				pieces.add(detective.piece());
			}
			return ImmutableSet.copyOf(pieces);
		}
		@Override
		public GameState advance(Move move){
			if(!moves.contains(move)) throw new IllegalArgumentException("Illegal move: "+move);
//			GameState nextGameState = move.accept(new Visitor<>(){
//				@Override public GameState visit(SingleMove singleMove){
//					if (move.commencedBy() == mrX.piece()){
//						List<LogEntry> logEntries = List.copyOf(log);
//						if (setup.moves.get(setup.moves.size() -1)){
//							logEntries.add(LogEntry.reveal(singleMove.ticket,singleMove.destination));
//						}
//						else logEntries.add(LogEntry.hidden(singleMove.ticket));
//						mrX = mrX.use(singleMove.ticket);
//						mrX = mrX.at(singleMove.destination);
//						return new MyGameState(setup,
//								ImmutableSet.copyOf(),
//								ImmutableList.copyOf(logEntries),
//								mrX, detectives);
//					}
//					return null;
//				}
//				@Override public GameState visit(DoubleMove doubleMove){
//					return null;
//				}
//			});
//			if( move.commencedBy() == this.mrX.piece()){
//				List<LogEntry> newLog = List.copyOf(this.log);
//
//			}

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
				}
			}
			return Optional.empty();
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
		public ImmutableSet<Move> getAvailableMoves() {
			Set<Move> currentMoves = new HashSet<>();
			if (this.remaining.contains(this.mrX.piece())) {
				currentMoves.addAll(makeSingleMoves(this.setup, this.detectives, this.mrX, this.mrX.location()));
				currentMoves.addAll(makeSingleMoves(this.setup, this.detectives, this.mrX, this.mrX.location()));
			} else {
				for (Player detective : this.detectives) {
					currentMoves.addAll(makeSingleMoves(this.setup, this.detectives, detective, detective.location()));
				}
				return ImmutableSet.copyOf(currentMoves);
			}
		}

		private static Set<SingleMove> makeSingleMoves(GameSetup setup, List<Player> detectives, Player player, int source){
			Set<SingleMove> singleMoves = new HashSet<>();
			for(int destination : setup.graph.adjacentNodes(source)) {
				boolean occupied = false;
				for (Player detective : detectives){
					if (detective.location()==destination){
						occupied = true;
					}
				}
				if (!occupied){
					for(Transport t : setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of()) ) {
						if (player.has(t.requiredTicket())){
							singleMoves.add(new SingleMove(player.piece(),source,t.requiredTicket(),destination));
							if (player.has(Ticket.SECRET)){
								singleMoves.add(new SingleMove(player.piece(),source,Ticket.SECRET,destination));
							}
						}
					}
				}
			}
			return singleMoves;
		}

		private static Set<DoubleMove> makeDoubleMoves(GameSetup setup, List<Player> detectives, Player player, int source){
			Set<DoubleMove> doubleMoves = new HashSet<>();
			Set<SingleMove> firstMoves = makeSingleMoves(setup,detectives,player,source);
			for (SingleMove firstMove : firstMoves){
				Set<SingleMove> secondMoves = makeSingleMoves(setup,detectives,player,firstMove.destination);
				for (SingleMove secondMove : secondMoves){
					if (!(secondMove.destination == source)){
						DoubleMove move = new DoubleMove(player.piece(),
								source,
								firstMove.ticket,
								firstMove.destination,
								secondMove.ticket,
								secondMove.destination);
						doubleMoves.add(move);
					}

				}
			}
			return doubleMoves;
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
