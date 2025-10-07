// Fix performance issue in React Native jest setup
global.performance = global.performance || {
  now: () => Date.now(),
};

