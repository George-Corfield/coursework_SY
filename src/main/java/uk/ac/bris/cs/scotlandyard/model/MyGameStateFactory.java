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
			this.winner = getWinner();
//			System.out.println(this.moves);
//			System.out.println(this.remaining);
//			System.out.println(this.winner);
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

		public List<Piece> getDetectivePieces(){
			List<Piece> detectivePieces = new ArrayList<>();
			for (Player player:this.detectives){
				detectivePieces.add(player.piece());
			}
			return detectivePieces;
		}
		public LogEntry setLog(Ticket t, int dest, int logSize){
					if (setup.moves.get(logSize)) return LogEntry.reveal(t,dest);
					else return LogEntry.hidden(t);
				}
		@Override
		public GameState advance(Move move){
			if(!moves.contains(move)) throw new IllegalArgumentException("Illegal move: "+move);
			GameState nextGameState = move.accept(new Visitor<GameState>(){
				@Override public GameState visit(SingleMove singleMove){
					if (move.commencedBy() == mrX.piece()){
						List<LogEntry> logEntries = new ArrayList<>(log);
						logEntries.add(setLog(singleMove.ticket,singleMove.destination, logEntries.size()));
						mrX = mrX.use(singleMove.ticket);
						mrX = mrX.at(singleMove.destination);
						List<Piece> remainingDetectives = getDetectivePieces();
						return new MyGameState(setup,
								ImmutableSet.copyOf(remainingDetectives),
								ImmutableList.copyOf(logEntries),
								mrX, detectives);
					} else {
						int detectiveIndex = getDetectivePieces().indexOf(move.commencedBy());
						Player detective = detectives.get(detectiveIndex);
						detective = detective.use(singleMove.ticket);
						detective = detective.at(singleMove.destination);
						List<Player> newDetectives = new ArrayList<>(detectives);
						newDetectives.set(detectiveIndex,detective);
						mrX = mrX.give(singleMove.ticket);
						List<Piece> remainingDetectives = new ArrayList<>(remaining);
						remainingDetectives.remove(move.commencedBy());
						remaining = ImmutableSet.copyOf(remainingDetectives);
						if (remainingDetectives.isEmpty() || getAvailableMoves().isEmpty()) remainingDetectives = List.of(mrX.piece());
						return new MyGameState(setup,
								ImmutableSet.copyOf(remainingDetectives),
								log, mrX, newDetectives);
					}

				}
				@Override public GameState visit(DoubleMove doubleMove){
					List<LogEntry> logEntries = new ArrayList<>(log);
					logEntries.add(setLog(doubleMove.ticket1,doubleMove.destination1, logEntries.size()));
					mrX = mrX.use(doubleMove.ticket1);
					logEntries.add(setLog(doubleMove.ticket2,doubleMove.destination2, logEntries.size()));
					mrX = mrX.use(doubleMove.ticket2);
					mrX = mrX.use(Ticket.DOUBLE);
					mrX = mrX.at(doubleMove.destination2);
					List<Piece> remainingDetectives = getDetectivePieces();
					return new MyGameState(setup,
							ImmutableSet.copyOf(remainingDetectives),
							ImmutableList.copyOf(logEntries),
							mrX, detectives);
				}
			});
			return nextGameState;
		}
		@Override
		public Optional<Integer> getDetectiveLocation(Detective detective){
			for(Player player : this.detectives){
				if (player.piece() == detective){
					return Optional.of(player.location());
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

		public boolean isMrxTurn(){
			return this.remaining.contains(this.mrX.piece());
		}

		public boolean noMovesLeft(){
			return this.log.size()==this.setup.moves.size();
		}

		public boolean hasTickets(Player detective){
			boolean noTickets = true;
			for (Ticket t : Ticket.values()) {
				if (detective.tickets().get(t) > 0){
					noTickets = false;
					break;
				}
			}
			return !noTickets;
		}
		@Override
		public ImmutableSet<Piece> getWinner(){
			boolean noPossibleMoves = true;
			for (Player detective: this.detectives){
				if (detective.location() == this.mrX.location()){
					return ImmutableSet.copyOf(getDetectivePieces());
				}
				if (hasTickets(detective)) {
					noPossibleMoves = false;
				}
			}
			if (noPossibleMoves){
				return ImmutableSet.of(this.mrX.piece());
			}
			if (noMovesLeft()){
				return ImmutableSet.of(this.mrX.piece());
			}
			if (this.moves.isEmpty() && this.isMrxTurn()){
				return ImmutableSet.copyOf(getDetectivePieces());
			}
			if (this.moves.isEmpty() && !this.remaining.isEmpty() && !this.isMrxTurn()){
				return ImmutableSet.of(this.mrX.piece());
			}
			//TODO detective win if detective move finish on mrx or mrx can't travel
			//TODO mrx win if mrx fills log and detectives haven't caught him or detectives cannot move
			return ImmutableSet.of();
		}
		@Override
		public ImmutableSet<Move> getAvailableMoves() {
			Set<Move> currentMoves = new HashSet<>();
			if (this.winner == null || this.winner.isEmpty())
				if (this.isMrxTurn() && !this.noMovesLeft()) {
					currentMoves.addAll(makeSingleMoves(this.setup, this.detectives, this.mrX, this.mrX.location()));
					if ((this.setup.moves.size() - this.log.size() >= 2) && (this.mrX.has(Ticket.DOUBLE))) {
						currentMoves.addAll(makeDoubleMoves(this.setup, this.detectives, this.mrX, this.mrX.location()));
					}
				} else {
					for (Player detective : this.detectives) {
						if (this.remaining.contains(detective.piece()))
							currentMoves.addAll(makeSingleMoves(this.setup, this.detectives, detective, detective.location()));
					}

				}
			return ImmutableSet.copyOf(currentMoves);
		}

		private static Set<SingleMove> makeSingleMoves(GameSetup setup, List<Player> detectives, Player player, int source){
			Set<SingleMove> singleMoves = new HashSet<>();
			for(int destination : setup.graph.adjacentNodes(source)) {
				boolean occupied = false;
				for (Player detective : detectives){
					if (detective.location()==destination){
						occupied = true;
						break;
					}
				}
				if (!occupied){
					for(Transport t : setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of()) ) {
						if (player.has(t.requiredTicket())){
							singleMoves.add(new SingleMove(player.piece(),source,t.requiredTicket(),destination));
						}
						if (player.has(Ticket.SECRET)){
							singleMoves.add(new SingleMove(player.piece(),source,Ticket.SECRET,destination));
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
				Set<SingleMove> secondMoves = makeSingleMoves(setup,detectives,player.use(firstMove.ticket),firstMove.destination);
				for (SingleMove secondMove : secondMoves){
					DoubleMove move = new DoubleMove(player.piece(),
								source,
								firstMove.ticket,
								firstMove.destination,
								secondMove.ticket,
								secondMove.destination);
					doubleMoves.add(move);
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
