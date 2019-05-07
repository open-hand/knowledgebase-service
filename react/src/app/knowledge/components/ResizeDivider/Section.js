import React, { Component } from 'react';
import PropTypes from 'prop-types';
import './Section.css';

class Section extends Component {
  render() {
    const { type, size } = this.props;
    const { width, height } = size;
    return (
      <div {...this.props} style={{ width: width || 'auto', height: height || 'auto' }} className={`Section Section-${type}`} />
    );
  }
}

Section.propTypes = {
  // bind: PropTypes.string.isRequired,
};

export default Section;
