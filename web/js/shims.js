function shim(name, val) {
  define(name, ['exports'], function(exports) {
    exports['default'] = val;
  });
}
shim('mori', mori);
shim('React', React);
