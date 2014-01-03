/**
 * @jsx React.DOM
 */

import service from "/service";
import _ from "underscore";

var cardWidthPixels = 290;
var cardMarginPixels = 5;

 export default React.createClass({
  getInitialState: function() {
    return {windowWidth: window.innerWidth};
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

      var label = mori.get(image, 'label');
      var description = mori.get(image, 'description');
      return (
        <li className="image-grid__item">
          <a href={mori.get(image, 'link')} target={mori.get(image, 'target') || ''}>
            <img src={mori.get(image, 'src') + '?width=' + width}
                 style={imgStyle}/>
          </a>
          {label || description ?
            <div className="image-grid__item__description">
              {description ?
                <h3>{mori.get(image, 'description')}</h3>
                : null}
              {label ?
                <h2>{label}</h2>
                : null}
            </div>
            : null}
        </li>
      );
    }, this.props.images);

    return (
      <ul className="image-grid">
        {mori.clj_to_js(items)}
      </ul>
    );
  },

  handleResize: _.throttle(function(e) {
    this.setState({windowWidth: window.innerWidth});
  }, 1000 /* ms */ / 60 /* fps */),

  resize: function(rootNode) {
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
  },

  componentDidMount: function(rootNode) {
    window.addEventListener('resize', this.handleResize);
    this.resize(rootNode);
  },

  componentDidUpdate: function(prevProps, prevState, rootNode) {
    this.resize(rootNode);
  },

  componentWillUnmount: function() {
    window.removeEventListener('resize', this.handleResize);
  }
 });
