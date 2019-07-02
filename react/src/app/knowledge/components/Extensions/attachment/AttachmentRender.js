import React, { Component } from 'react';
import { withRouter } from 'react-router-dom';

class AttachmentRender extends Component {
  constructor(props, context) {
    super(props, context);
    this.state = {
    };
  }

  componentDidMount() {
  }

  componentWillUnmount() {
  }

  render() {
    const { name } = this.props;

    return (
      <a href="http://www.baidu.com">{name}</a>
    );
  }
}
export default withRouter(AttachmentRender);
