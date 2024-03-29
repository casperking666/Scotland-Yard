package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

import java.util.ArrayList;
import java.util.List;

/**
 * cw-model
 * Stage 2: Complete this class
 */
public final class MyModelFactory implements Factory<Model> {

	private final class MyModel implements Model {

		private final List<Observer> observers;
		private final Board.GameState gameState;

		private MyModel(
				final GameSetup setup,
				final Player mrX,
				final List<Player> detectives) {
			this.observers = new ArrayList<>();
			this.gameState = new MyGameStateFactory().build(setup, mrX, ImmutableList.copyOf(detectives));
		}

		@Nonnull
		@Override
		public Board getCurrentBoard() {
			return gameState;
		}

		@Override
		public void registerObserver(@Nonnull Observer observer) {
			if (observer == null) throw new NullPointerException();
			for (Observer singleObserver : observers)
				if (observer.equals(singleObserver))
					throw new IllegalArgumentException();
			observers.add(observer);
		}

		@Override
		public void unregisterObserver(@Nonnull Observer observer) {
			if (observer == null) throw new NullPointerException();
			boolean isAvailable = false;
			for (Observer singleObserver : observers)
				if (singleObserver.equals(observer)) isAvailable = true;
			if (isAvailable)
				observers.remove(observer);
			else throw new IllegalArgumentException();
		}

		@Nonnull
		@Override
		public ImmutableSet<Observer> getObservers() {
			return ImmutableSet.copyOf(observers);
		}


		// Inform all the observers the state change when the current move has been made.
		@Override
		public void chooseMove(@Nonnull Move move) {
			gameState.advance(move);
			for (Observer observer : observers) {
				if (gameState.getWinner().isEmpty())
					observer.onModelChanged(gameState, Observer.Event.MOVE_MADE);
				else observer.onModelChanged(gameState, Observer.Event.GAME_OVER);
			}
		}
	}

	@Nonnull @Override public Model build(GameSetup setup,
	                                      Player mrX,
	                                      ImmutableList<Player> detectives) {
		return new MyModel(setup, mrX, detectives);
	}


}
