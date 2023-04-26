package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import java.util.*;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

import java.util.ArrayList;

/**
 * cw-model
 * Stage 2: Complete this class
 */
public final class MyModelFactory implements Factory<Model> {

	@Nonnull @Override public Model build(GameSetup setup,
	                                      Player mrX,
	                                      ImmutableList<Player> detectives) {
		return new Model() {
			public Board.GameState gameState = new MyGameStateFactory().build(setup,mrX,detectives);
			public ImmutableList<Observer> observers = ImmutableList.of();

			@Nonnull
			@Override
			public Board getCurrentBoard() {
				return gameState;
			}

			@Override
			public void registerObserver(@Nonnull Observer observer) {
				List<Observer> observerList = new ArrayList<Observer>(observers);
				if (observer == null) throw new NullPointerException();
				if (!observerList.contains(observer)) observerList.add(observer);
				else throw new IllegalArgumentException();
				observers = ImmutableList.copyOf(observerList);
			}

			@Override
			public void unregisterObserver(@Nonnull Observer observer) {
				List<Observer> observerList = new ArrayList<Observer>(observers);
				if (observer == null) throw new NullPointerException();
				if (observerList.contains(observer)) observerList.remove(observer);
				else throw new IllegalArgumentException();
				observers = ImmutableList.copyOf(observerList);
			}

			@Nonnull
			@Override
			public ImmutableSet<Observer> getObservers() {
				return ImmutableSet.copyOf(observers);
			}

			@Override
			public void chooseMove(@Nonnull Move move) {
				gameState = gameState.advance(move);
				for (Observer observer: observers){
					if (gameState.getWinner().isEmpty()) {
						observer.onModelChanged(getCurrentBoard(), Observer.Event.MOVE_MADE);
					}
					else {
						observer.onModelChanged(getCurrentBoard(), Observer.Event.GAME_OVER);
					}
				}
			}
		};
	}
}
