package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import com.sun.javafx.geom.Edge;
import io.atlassian.fugue.Option;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.*;

public class NoobSlayer666 implements Ai {

	private List<Optional<Integer>> detectiveLocations;
	private Integer mrxLocation;
	private HashMap<Move, Integer> scores;
	private List<Move> mrxMoves;
	private Integer[] closestNodes;

	// a good AI name is the key of success
	@Nonnull @Override public String name() { return "NoobSlayer666"; }


	// After all the processing done in the scoring method, the method picks a random move from a limited selections
	@Nonnull @Override public Move pickMove(
			@Nonnull Board board,
			Pair<Long, TimeUnit> timeoutPair) {
		mrxMoves = board.getAvailableMoves().asList();
		movesProcessor(board);
		var moveScore = new ArrayList<Move>(scores.keySet());
		if (moveScore.size() == 0) return mrxMoves.get(new Random().nextInt(mrxMoves.size()));
		return moveScore.get(new Random().nextInt(moveScore.size()));
	}


	// Process the availableMoves in the following ways
	// 1. Make sure that mrX doesn't go to the nodes where it will be caught by detectives straight away
	// meaning it won't stumble into detectives next availableMoves.
	// 2. Calculating the distances between mrX and two closest detectives. If they ever come too close,
	// mrX will use doubleMoves to escape as far of a range as possible.
	// 3. During the reveal round, mrX will select a node where it has the highest degree so it increases
	// his flexibility of moving given that all the previous conditions still hold.
	public void movesProcessor(Board board) {

		// initialisations, getting the needed information
		getPlayersLocation(board);
		scores = new HashMap<>();
		for (Move move : mrxMoves)
			scores.put(move, 0);
		Board.GameState gameState = (Board.GameState) board;
		gameState = gameState.advance(mrxMoves.get(new Random().nextInt(mrxMoves.size())));
		List<Move> detectiveMoves = gameState.getAvailableMoves().asList(); //
		ImmutableList<LogEntry> log = board.getMrXTravelLog();
		int size = log.size();

		// visitor patterns; to get mrX's possible destinations and their respective move type
		Function<Move.SingleMove, Integer> smf1 = x -> x.destination;
		Function<Move.DoubleMove, Integer> dmf2 = x -> x.destination2;
		Move.FunctionalVisitor<Integer> getDestination2 = new Move.FunctionalVisitor<>(smf1, dmf2);
		Function<Move.SingleMove, String> sm = x -> "SingleMove";
		Function<Move.DoubleMove, String> dm = x -> "DoubleMove";
		Move.FunctionalVisitor<String> getSingleOrDouble = new Move.FunctionalVisitor<>(sm, dm);

		// ticket check
		if (board.getPlayerTickets(Piece.MrX.MRX).get().getCount(ScotlandYard.Ticket.DOUBLE) == 0) {
			for (Move mrxMove : mrxMoves)
				if (mrxMove.visit(getSingleOrDouble).equals("DoubleMove"))
					scores.remove(mrxMove);
		}

		// 1. no silly moves
		for (Move mrxMove : mrxMoves) {
			for (Move detectiveMove : detectiveMoves)
				if (!board.getSetup().graph.edgeValueOrDefault(mrxMove.visit(getDestination2), detectiveMove.source(), ImmutableSet.of()).isEmpty())
					scores.remove(mrxMove);
		}

		// 2. uses doubleMoves in a described and smart way
		ImmutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> graph = board.getSetup().graph;
		for (Move mrxMove : mrxMoves) {
			if (escape(graph)) {
				if ((dijkstraSP(graph, closestNodes[0], mrxMove.visit(getDestination2)) + dijkstraSP(graph, closestNodes[1], mrxMove.visit(getDestination2))) < 5)
					scores.remove(mrxMove);
			}
			else {
				if (mrxMove.visit(getSingleOrDouble).equals("DoubleMove"))
					scores.remove(mrxMove);
			}
		}

		// 3. pick the move with the maximum degree when revealing
		if (board.getSetup().rounds.get(size)) {
			int maxDegree = 0;
			Move maxMove = null;
			for (Move mrxMove : scores.keySet()) {
				if (maxDegree < graph.degree(mrxMove.visit(getDestination2))) {
					maxDegree = graph.degree(mrxMove.visit(getDestination2));
					maxMove = mrxMove;
				}
			}
			scores.clear();
			if (maxMove != null) scores.put(maxMove, 0);
		}

	}


	// A method used to tell if mrX should use doubleMove to escape or not.
	public boolean escape(ImmutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> graph) {
		closestNodes = new Integer[4];
		TreeMap<Integer, Integer> distances = new TreeMap<>();
		for (Optional<Integer> location : detectiveLocations) {
			distances.put(dijkstraSP(graph, mrxLocation, location.get()), location.get());
		}
		if (distances.firstKey().equals(1) && distances.higherKey(1) <= 2) {
			closestNodes[0] = distances.get(distances.firstKey());
			closestNodes[1] = distances.get(distances.higherKey(1));
			return true;
		}
		else return false;
	}


	// Implementation of Dijkstra's algorithm with a change in which it only calculates the shortest distance
	// between the source and the target.
	public Integer dijkstraSP(ImmutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> graph,
						   Integer source, Integer target) {

		int size = graph.nodes().size();
		Integer[] distTo = new Integer[size];
		for (int v = 0; v < size; v++) {
			distTo[v] = Integer.MAX_VALUE;
		}
		distTo[source - 1] = 0;
		HashMap<Integer, Integer> nodeDist = new HashMap<>();
		for (Integer node : graph.nodes()) {
			nodeDist.put(node, Integer.MAX_VALUE);
		}
		nodeDist.put(source, 0);
		Integer shortestDistance;
		while (!nodeDist.isEmpty()) {
			Integer currentNode = 0;
			int temp = Integer.MAX_VALUE;
			for (Integer key : nodeDist.keySet()) { // find a node with the minimum dist[u]
				if (distTo[key - 1] < temp) {
					temp = distTo[key - 1];
					currentNode = key;
				}
			}
			nodeDist.remove(currentNode);
			if (currentNode.equals(target)) {
				shortestDistance = distTo[currentNode - 1];
				return shortestDistance;
			}
			for (Integer v : graph.adjacentNodes(currentNode)) {
				Integer alt = distTo[currentNode - 1] + 1;
				if (alt < distTo[v - 1])
					distTo[v - 1] = alt;
			}
		}
		return -1;
	}


	public void getPlayersLocation(Board board) {
		detectiveLocations = new ArrayList<>();
		for (Piece player : board.getPlayers()) {
			if (player.isMrX()) mrxLocation = mrxMoves.get(0).source();
			for (Piece.Detective detective : Piece.Detective.values())
				if (detective.equals(player))
					detectiveLocations.add(board.getDetectiveLocation(detective));
		}
	}


}


