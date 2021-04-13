package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.*;
import javax.annotation.Nonnull;
import java.util.function.Function;

import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.Move.*;
import uk.ac.bris.cs.scotlandyard.model.Piece.*;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;

/**
 * cw-model
 * Stage 1: Complete this class
 */
public final class MyGameStateFactory implements Factory<GameState> {
	public int count = 0;
	private final class MyGameState implements GameState {

		private GameSetup setup;
		private ImmutableSet<Piece> remaining;
		private ImmutableList<LogEntry> log;
		private Player mrX;
		private List<Player> detectives;
		private ImmutableList<Player> everyone;
		private ImmutableSet<Move> moves;
		private ImmutableSet<Piece> winner;
		private Set<Piece> people; // i created it
		int cnt = 1;// by Eric
		int nt = 0;



		private MyGameState(
				final GameSetup setup,
				final ImmutableSet<Piece> remaining,
				final ImmutableList<LogEntry> log,
				final Player mrX,
				final List<Player> detectives) {
			this.setup = setup;
			this.remaining = remaining;
			this.log = log;
			this.mrX = mrX;
			this.detectives = detectives;
		}


		@Override
		public GameSetup getSetup() {
			return this.setup;
		}

		@Override
		public ImmutableSet<Piece> getPlayers() {
			/* this.people = new HashSet<>();
			for (Player person : detectives)
				this.people.add(person.piece());
			this.people.add(mrX.piece());
			ImmutableSet<Piece> people = ImmutableSet.copyOf(this.people);
			return people;*/
			ArrayList<Player> everyone = new ArrayList<>();
			for (Player detective : detectives)
				everyone.add(detective);
			everyone.add(mrX);
			this.everyone = ImmutableList.copyOf(everyone);
			Set<Piece> people = new HashSet<>();
			for (Player person : everyone)
				people.add(person.piece());
			return ImmutableSet.copyOf(people);
		}

		@Nonnull
		@Override
		public Optional<Integer> getDetectiveLocation(Detective detective) {
			for(int i = 0; i < detectives.size(); i++) {
				if(detectives.get(i).piece().webColour().equals(detective.webColour())) return Optional.of(detectives.get(i).location());
			}
			return Optional.empty();
		}

		@Nonnull
		@Override

		public Optional<TicketBoard> getPlayerTickets(Piece piece) {
			if(mrX.piece().equals(piece)) {
				TicketBoard result = new TicketBoard(){
					@Override
					public int getCount(Ticket ticket) {
						return mrX.tickets().getOrDefault(ticket, 0);
					}
				};
				return Optional.of(result);
			}

			for(Player rst : detectives) {
				if(rst.piece().equals(piece)) {
					TicketBoard result = new TicketBoard(){
						@Override
						public int getCount(Ticket ticket) {
							return rst.tickets().getOrDefault(ticket, 0);
						}
					};
					return Optional.of(result);
				}
			}
			return Optional.empty();
		}

		@Nonnull
		@Override
		public ImmutableList<LogEntry> getMrXTravelLog() {
			return log;
		}

		@Nonnull
		@Override
		public ImmutableSet<Piece> getWinner() {
			// Detectives win the game
			for(Player test : detectives){
				if(test.location() == mrX.location()){
					var detectiveWinner = new HashSet<Piece>();
					for(int i = 0; i < detectives.size(); i++){
						detectiveWinner.add(detectives.get(i).piece());
					}
					return winner = ImmutableSet.copyOf(detectiveWinner);
				}
			}
			// Mister X wins the game: All detectives can NO longer move the pieces.
			Boolean detectivesCantMove = false;
			for(int i = 0; i < detectives.size(); i++){
				//needed something about AvailableMoves of All Detectives are empty!
			}

			//Mister X wins the game: MrX survives for 22 rounds (?which means cnt = 25 because there are 2 Double?)

			if(cnt == 25){ // cnt=25 because the round 24 is only over when all detectives complete their moves!
				return winner = ImmutableSet.of(mrX.piece());
			}
			return winner = ImmutableSet.of();
		}

		@Nonnull
		@Override
		public ImmutableSet<Move> getAvailableMoves() {
			ArrayList<Move> container = new ArrayList<>();
			if (remaining.contains(mrX.piece())) {
				if (setup.rounds.size() - log.size() > 1) {
					container.addAll(makeDoubleMoves(setup, detectives, mrX, mrX.location()));
				}
				container.addAll(makeSingleMoves(setup, detectives, mrX, mrX.location()));
			}
			else {
				for (Player detective : detectives) {
					if (remaining.contains(detective.piece()))
						container.addAll(makeSingleMoves(setup, detectives, detective, detective.location()));
				}
			}
			moves = ImmutableSet.copyOf(container);
			return moves;
		}



		@Override public GameState advance(Move move) {
			moves = getAvailableMoves(); // have to have this to make it work
			if(!moves.contains(move)) throw new IllegalArgumentException("Illegal move: "+move);


			//Need to return for the Constructor
			//	final GameSetup setup,
			//	final ImmutableSet<Piece> remaining,
			//	Finished! final ImmutableList<LogEntry> log,
			//	final Player mrX,
			//	final List<Player> detectives)

			List<Player> newDetectives = new ArrayList<>();
			newDetectives.addAll(detectives);

			// applied visitor pattern, taking out the destination as the new source
			Integer destination = null;
			Integer destination1 = null;
			Integer destination2 = null;

			Function<SingleMove, String> sm = x -> "SingleMove";
			Function<DoubleMove, String> dm = x -> "DoubleMove";
			FunctionalVisitor<String> getSingleOrDouble = new FunctionalVisitor<>(sm, dm);

			Function<SingleMove, Integer> smf1 = x -> x.destination;
			Function<DoubleMove, Integer> dmf1 = x -> x.destination1;
			FunctionalVisitor<Integer> getDestination = new FunctionalVisitor<>(smf1, dmf1);

			Function<SingleMove, Integer> smf2 = x -> x.destination; // think this is repetitive
			Function<DoubleMove, Integer> dmf2 = x -> x.destination2;
			FunctionalVisitor<Integer> getDestination2 = new FunctionalVisitor<>(smf2, dmf2);

			Function<SingleMove, Ticket> smt1 = x -> x.ticket;
			Function<DoubleMove, Ticket> dmt1 = x -> x.ticket1;
			FunctionalVisitor<Ticket> getTicket1 = new FunctionalVisitor<>(smt1, dmt1);


			Function<SingleMove, Ticket> smt2 = x -> x.ticket; // same as this
			Function<DoubleMove, Ticket> dmt2 = x -> x.ticket2;
			FunctionalVisitor<Ticket> getTicket2 = new FunctionalVisitor<>(smt2, dmt2);
			//Integer destination = move.visit(getDestination);
			if(move.visit(getSingleOrDouble).equals("SingleMove")){
				destination = move.visit(getDestination);
			}
			else if(move.visit(getSingleOrDouble).equals("DoubleMove")){
				destination1 = move.visit(getDestination);
				destination2 = move.visit(getDestination2);
			}


			// Tickets used and handed
			if(move.commencedBy().isDetective()){
				for(int i = 0; i < newDetectives.size(); i++){
					if(newDetectives.get(i).piece().equals(move.commencedBy())){
						// newDetectives.set(i, newDetectives.get(i).use(move.visit(getTicket1)));
						Player tempDetective = newDetectives.get(i);
						newDetectives.set(i, tempDetective.use(move.visit(getTicket1)));
						detectives = ImmutableList.copyOf(newDetectives);
						mrX = mrX.give(move.visit(getTicket1));
					}
				}
			}
			else if(move.commencedBy().isMrX()){
				if(move.visit(getSingleOrDouble).equals("SingleMove")){
					mrX = mrX.use(move.visit(getTicket1));
				} else if(move.visit(getSingleOrDouble).equals("DoubleMove")){
					mrX = mrX.use(Ticket.DOUBLE);
					mrX = mrX.use(move.visit(getTicket1));
					mrX = mrX.use(move.visit(getTicket2));
				}
			}
			// End of Tickets used and handed

			//Player locations
			if(move.commencedBy().isMrX()){
				if(move.visit(getSingleOrDouble).equals("SingleMove")){
					mrX = mrX.at(destination);
				} else if(move.visit(getSingleOrDouble).equals("DoubleMove")){
					mrX = mrX.at(destination2);
				}
			}
			else if(move.commencedBy().isDetective()){
				for(int i = 0; i < newDetectives.size(); i++){
					if(newDetectives.get(i).piece().equals(move.commencedBy())){
						newDetectives.set(i, newDetectives.get(i).at(destination));
						detectives = ImmutableList.copyOf(newDetectives);
					}
				}
			}


			// Travel Log
			/*
			if(move.commencedBy().isMrX()){
				var aLog = new ArrayList<LogEntry>();
				aLog.addAll(this.log);
				if(move.visit(getSingleOrDouble).equals("SingleMove")) {
					if(!setup.rounds.get(nt)){
						aLog.add(LogEntry.hidden(move.visit(getTicket1)));
					} else if(setup.rounds.get(nt)){
						aLog.add(LogEntry.reveal(move.visit(getTicket1), destination));
					}nt++;
				}
				else if(move.visit(getSingleOrDouble).equals("DoubleMove")){
					//for the 1st move
					if(!setup.rounds.get(nt)){
						aLog.add(LogEntry.hidden(move.visit(getTicket1)));
					} else if(setup.rounds.get(nt)){
						aLog.add(LogEntry.reveal(move.visit(getTicket1), destination1));
					}nt++;
					//for the 2nd move
					if(!setup.rounds.get(nt)){
						aLog.add(LogEntry.hidden(move.visit(getTicket2)));
					} else if(setup.rounds.get(nt)) {
						aLog.add(LogEntry.reveal(move.visit(getTicket2), destination2));
					}nt++;
				}
				this.log = ImmutableList.copyOf(aLog);
			}*/

			if (move.commencedBy().isMrX()) {
				int size = log.size();
				var tempLog = new ArrayList<LogEntry>();
				tempLog.addAll(this.log);
				if (move.visit(getSingleOrDouble).equals("SingleMove")) {
					if (setup.rounds.get(size)) {
						tempLog.add(LogEntry.reveal(move.visit(getTicket1), destination));
					}
					else tempLog.add(LogEntry.hidden(move.visit(getTicket1)));
				}
				else {
					if (setup.rounds.get(size)) {
						tempLog.add(LogEntry.reveal(move.visit(getTicket1), destination1));
					}
					else {tempLog.add(LogEntry.hidden(move.visit(getTicket1)));}
					// size is 1 bigger because the log size won't be updated here, but since we just add
					// another logEntry above, the size hence should add 1 to it.
					if (setup.rounds.get(size + 1)) {
						tempLog.add(LogEntry.reveal(move.visit(getTicket2), destination2));
					}
					else {tempLog.add(LogEntry.hidden(move.visit(getTicket2)));}
				}
				this.log = ImmutableList.copyOf(tempLog);
			}
			//Travel Log ends

			// might still have bugs, please don't change anything
			var container = new ArrayList<Piece>();
			container.addAll(remaining);
			if (move.commencedBy().isMrX()) {
				container.remove(MrX.MRX);
				for (Player detective : detectives) {
					container.add(detective.piece());
				}
			}
			if (move.commencedBy().isDetective()) {
				container.remove(move.commencedBy());
			}
			if (container.isEmpty()) {
				container.add(MrX.MRX);
			}
			remaining = ImmutableSet.copyOf(container);

			GameState state = new MyGameState(this.setup, this.remaining, this.log, this.mrX, this.detectives);

			return state;
		}
	}


	private static ImmutableSet<SingleMove> makeSingleMoves(
			GameSetup setup,
			List<Player> detectives,
			Player player,
			int source) {
		final var singleMoves = new ArrayList<SingleMove>();
		for (int destination : setup.graph.adjacentNodes(source)) {
			boolean isAvailable = true;
			for (Player detective : detectives) {
				if (destination == detective.location()) {
					isAvailable = false;
				}
			}
			if (isAvailable)
				for (Transport t : setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of())) {
					if (player.has(t.requiredTicket())) {
						SingleMove singleMove = new SingleMove(player.piece(), source, t.requiredTicket(), destination);
						singleMoves.add(singleMove);
					}
					if (player.has(Ticket.SECRET)) {
						SingleMove singleMove = new SingleMove(player.piece(), source, Ticket.SECRET, destination);
						singleMoves.add(singleMove);
					}
				}
		}
		return ImmutableSet.copyOf(singleMoves);
	}


	private static ImmutableSet<DoubleMove> makeDoubleMoves(
			GameSetup setup,
			List<Player> detectives,
			Player player,
			int source) {
		final var doubleMoves = new ArrayList<DoubleMove>();
		if (!player.has(Ticket.DOUBLE)) return ImmutableSet.of();
		ImmutableSet<SingleMove> singleMoves = makeSingleMoves(setup, detectives, player, source);
		for (SingleMove singleMove : singleMoves) {
			Set<SingleMove> secondMoves = new HashSet<>();
			secondMoves.addAll(makeSingleMoves(setup, detectives, player.use(singleMove.ticket), singleMove.destination));
			for (SingleMove secondMove : secondMoves) {
				DoubleMove doubleMove = new DoubleMove(
						singleMove.commencedBy(),
						singleMove.source(),
						singleMove.ticket,
						singleMove.destination,
						secondMove.tickets().iterator().next(), //  haven't revise on this yet
						secondMove.destination
				);
				doubleMoves.add(doubleMove);
			}
		}
		return ImmutableSet.copyOf(doubleMoves);
	}


	@Nonnull @Override public GameState build(
			GameSetup setup,
			Player mrX,
			ImmutableList<Player> detectives) {
		if (setup == null || mrX == null || detectives == null) { throw new NullPointerException(); }
		if (!mrX.isMrX()) { throw new IllegalArgumentException(); }
		for(int i = 0; i < detectives.size(); i++) {
			if(detectives.get(i).piece().isMrX() && mrX.isDetective()) throw new IllegalArgumentException("Swapped!");
		}
		for (Player player : detectives)
			if (!player.isDetective())
				throw new IllegalArgumentException();
		for (int i = 0; i < detectives.size(); i++)
			for (int j = 0; j < detectives.size(); j++) {
				if (detectives.get(i).piece().equals(detectives.get(j).piece()) && i != j)
					throw new IllegalArgumentException();
				if (detectives.get(i).location() == detectives.get(j).location() && i != j)
					throw new IllegalArgumentException(); }
		for (Player player : detectives)
			if (player.has(Ticket.SECRET) || player.has(Ticket.DOUBLE))
				throw new IllegalArgumentException();
		if (setup.rounds.isEmpty()) throw new IllegalArgumentException("Rounds is empty!");
		if (setup.graph.nodes().isEmpty()) throw new IllegalArgumentException();
		return new MyGameState(setup, ImmutableSet.of(MrX.MRX), ImmutableList.of(), mrX, detectives);
	}

}
