import React from 'react';
import api from '../util/api';

export default class CrawlerUi extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      startTime: null,
      stopTime: null,
      running: null,
      exception: null,
      runningUrlCount: null
    };
  }

  render() {
    return (
      <div className="Crawler-ui">
        <button onClick={this.start}>Start</button>
        <button onClick={this.stop}>Stop</button>
        <button onClick={this.getStatus}>Update</button>
        <h3>Running url count: {this.state.runningUrlCount}</h3>
        <h3>Is running: {this.state.running ? 'Yes' : 'No'}</h3>
      </div>
    );
  }

  start = (e) => {
    api.get("/crawler/start").then(response => this.getStatus())
  };

  stop = (e) => {
    api.get("/crawler/stop").then(response => this.getStatus())
  };

  getStatus = (e) => {
    api.get("/crawler/status").then(response => this.setState(prevState => response.data))
  };
}
