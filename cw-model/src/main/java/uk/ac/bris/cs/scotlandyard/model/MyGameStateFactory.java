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


		ImmutableList<Boolean> rounds = ImmutableList.of(false, false, true, false, false, false, false, true,
				false, false, false, false, true, false, false, false, false, true, false, false, false, false, false, true);

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
			this.winner = ImmutableSet.of();
			return this.winner;
		}

		@Nonnull
		@Override
		public ImmutableSet<Move> getAvailableMoves() {
			ArrayList<Move> container = new ArrayList<>();
			if (remaining.contains(mrX.piece())) { // remaining works as a trigger to tell which one to deliver
				if (setup.rounds.size() - log.size() > 1) {
					container.addAll(makeDoubleMoves(setup, detectives, mrX, mrX.location()));
				}
				container.addAll(makeSingleMoves(setup, detectives, mrX, mrX.location()));
			};
			if (remaining.size() > 1) { // unfinished
				for (Player detective : detectives)
					container.addAll(makeSingleMoves(setup, detectives, detective, detective.location()));
			}
			moves = ImmutableSet.copyOf(container);
			return moves;
		}

		//test
		public void smsm(Boolean bl){bl = !bl;}

		@Override public GameState advance(Move move) {
			if(!moves.contains(move)) throw new IllegalArgumentException("Illegal move: "+move);


			//Need to return for the Constructor
			//	final GameSetup setup,
			//	final ImmutableSet<Piece> remaining,
			//	Finished! final ImmutableList<LogEntry> log,
			//	final Player mrX,
			//	final List<Player> detectives)


			//

			// Travel Log
			if(move.commencedBy().isMrX()){
				var aLog = new ArrayList<LogEntry>();
				aLog.addAll(this.log);
				if(ScotlandYard.REVEAL_ROUND.contains(cnt)){
					aLog.add(LogEntry.hidden(move.tickets().iterator().next()));
				}
				else if(!ScotlandYard.REVEAL_ROUND.contains(cnt)){
					aLog.add(LogEntry.reveal(move.tickets().iterator().next(), mrX.location()));
				}
				cnt++;
				this.log = ImmutableList.copyOf(aLog);
			}
			if (remaining.contains(mrX.piece())) {

			}
			//Travel Log ends

			// applied visitor pattern, taking out the destination as the new source
			Function<SingleMove, Integer> smf = x -> x.destination;
			Function<DoubleMove, Integer> dmf = x -> x.destination2;
			FunctionalVisitor<Integer> getDestination = new FunctionalVisitor<>(smf, dmf);
			Integer destination = move.visit(getDestination);

			// update remaining for my use
			var container = new ArrayList<Piece>();
			container.addAll(remaining);
			if (move.commencedBy().isDetective())
				container.add(move.commencedBy());
			remaining = ImmutableSet.copyOf(container);

			return null;
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
