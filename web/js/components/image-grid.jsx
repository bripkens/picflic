/**
 * @jsx React.DOM
 */

 import service from "/service";

var cardWidthPixels = 290;
var cardMarginPixels = 5;

 export default React.createClass({
  getInitialState: function() {
    return {windowWidth: window.innerWidth};
  },

  handleResize: _.throttle(function(e) {
    this.setState({windowWidth: window.innerWidth});
  }, 1000 /* ms */ / 60 /* fps */),

  componentDidMount: function() {
    window.addEventListener('resize', this.handleResize);
  },

  componentWillUnmount: function() {
    window.removeEventListener('resize', this.handleResize);
  },

  render: function() {
    var items = mori.map(function(image) {
      var resolution = service.getImageResolution(mori.get(image, 'image'),
        cardWidthPixels);

      var width = Math.min(cardWidthPixels, mori.get(resolution, 'width'));
      var height = mori.get(resolution, 'height') /
        mori.get(resolution, 'width') * width;

      var imgStyle = {
        width: width,
        height: height
      };

      return (
        <li className="image-grid__item">
          <img src={mori.get(image, 'src') + '?width=' + width} style={imgStyle}/>
          <div className="image-grid__item__description">
            {image.description ?
              <h3>{mori.get(image, 'description')}</h3>
              : null}
            <h2>{mori.get(image, 'label')}</h2>
          </div>
        </li>
      );
    }, this.props.images);

    return (
      <ul className="image-grid">
        {mori.clj_to_js(items)}
      </ul>
    );
  },

  componentDidUpdate: function(prevProps, prevState, rootNode) {
    var availableWidth = rootNode.clientWidth;
    var imageCount = mori.count(this.props.images);
    var cardOuterWidth = cardWidthPixels + cardMarginPixels * 2;
    var cardsPerRow = Math.floor(availableWidth / cardOuterWidth);
    var rowMargin = (availableWidth - cardsPerRow * cardOuterWidth) / 2;

    // rowHeight contains the current total height of each row
    var rowHeight = [];
    for (var i = 0; i < cardsPerRow; i++) {
      rowHeight[i] = cardMarginPixels;
    }

    var childNodes = rootNode.childNodes;
    var dimensions = [];
    for (var i = 0, n = childNodes.length; i < n; i++) {
      var childNode = childNodes[i];
      var column = i % cardsPerRow;
      var columnWidth = childNode.clientWidth;
      var columnHeight = childNode.clientHeight;
      dimensions.push({
        top: rowHeight[column] + 'px',
        left: cardOuterWidth * column + rowMargin + 'px'
      });
      rowHeight[column] = rowHeight[column] + columnHeight + cardMarginPixels * 2;
    }

    for (var i = 0, n = childNodes.length; i < n; i++) {
      var childNode = childNodes[i];
      var dimension = dimensions[i];
      childNode.style.top = dimension.top;
      childNode.style.left = dimension.left;
    }
  }
 });
