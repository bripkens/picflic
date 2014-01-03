function shim(name, val) {
  define(name, ['exports'], function(exports) {
    exports['default'] = val;
  });
}
shim('mori', mori);
shim('React', React);
shim('underscore', _);
shim('crossroads', crossroads);


Promise.prototype.done = function(msg) {
  msg = msg || 'Unhandled rejected promise:';
  this.then(null, function(err) {
    if (err instanceof Error) {
      console.error(msg, err.message, err);
    } else {
      console.error(msg, err);
    }
  });
}
