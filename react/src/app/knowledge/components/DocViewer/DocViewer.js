import React, { Component } from 'react';
import { withRouter } from 'react-router-dom';
import 'codemirror/lib/codemirror.css';
import 'tui-editor/dist/tui-editor.min.css';
import 'tui-editor/dist/tui-editor-contents.min.css';
import { Viewer } from '@toast-ui/react-editor';
import DocHeader from '../DocHeader';
import './DocViewer.scss';

class Hello extends Component {
  viewerRef = React.createRef();

  render() {
    return (
      <div className="c7n-docViewer">
        <DocHeader />
        <Viewer
          ref={this.viewerRef}
          initialValue={'意大利面之旅\n' +
          '      黑洞的密度令人难以置信，其引力比我们在地球上感受到的要大几百万倍。 如果你离黑洞太近，它庞大的引力会让你四分五裂。随着越靠越近，你头部和脚部的重力差异会变得巨大，你会像口香糖一样被拉扯的老长，然后被撕裂，科学家认为这就像是把人当成意大利面处置一样。最终，你变成了一股亚原子粒子，像水一样旋入黑洞之中。\n' +
          '      物理学家Neil De Grasse Tyson表示：“根据天文学，随着你越靠越近，重力也会自然而然地增加，在黑洞的拉力超过人体分子键所能承载的力之后，你也就四分五裂了。在那一刻，你的身体会被撕裂成两部分，你的一切都会被带入黑洞的中心。除此之外，你会被黑洞从时间和空间的片段中挤出去，就好像挤牙膏那样。”\n' +
          '\n' +
          '长生不死\n' +
          '      黑洞越大，其引力就越小。这让专家们开始思索，如果黑洞足够大，那么是否有可能其引力不足以让你像意大利面一样被拉扯的四分五裂。那么，除了四分五裂之外，还有什么结局呢？\n' +
          '      据说，由于极大的引力使时间和空间发生弯曲，时间在黑洞的边缘是冻结的。如果你恰好到达了时间被冻结的地方，却又没有四分五裂的话，你可能会永垂不朽。但是，你的生命只是相对于地球上的人而言会变得更长，然而你自己却感受不到这一点。你认为自己的寿命仍然处在正常范围。\n' +
          '      物理学家Viktor Toth解释说：“每当想起相对论中的时间膨胀理论时，请记住这个理论与你无关，它只是以旁观者的视角观察时得到的感受。也就是说，如果参照物是手表和日历，那么你的寿命仍然是人类的正常寿命。但与此同时，时光飞逝你被困黑洞的日子里，地球可能已经经历了几十亿年的变迁。然而在黑洞中的你感觉不到这种流逝，你以为自己只是像普通人类一样活了几十年而已。'}
        />
      </div>
    );
  }
}
export default withRouter(Hello);
