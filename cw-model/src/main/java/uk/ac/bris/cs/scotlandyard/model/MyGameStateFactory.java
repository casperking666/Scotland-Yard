package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.*;
import javax.annotation.Nonnull;
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
		public ImmutableSet<Piece> getPlayers() { // I have no idea what I am doing but I get it.
			this.people = new HashSet<>();
			for (Player person : detectives)
				this.people.add(person.piece());
			this.people.add(mrX.piece());
			ImmutableSet<Piece> people = ImmutableSet.copyOf(this.people);
			return people;
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
			return this.log;
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
			moves = ImmutableSet.copyOf(makeSingleMoves(setup, detectives, mrX, mrX.location()));
			return moves;
		}

		@Override public GameState advance(Move move) {  return null;  }
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
			GameState setup,
			List<Player> detectives,
			Player player,
			int source) {
		final var doubleMoves = new ArrayList<DoubleMove>();
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
