import mori from "mori";

var location = window.location;

export default mori.js_to_clj({
  'baseUrl': location.protocol + '//' + location.hostname + ':3000'
});
