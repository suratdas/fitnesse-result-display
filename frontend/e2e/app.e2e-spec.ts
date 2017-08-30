import { Angular2SeedFinalPage } from './app.po';

describe('angular2-seed-final App', function() {
  let page: Angular2SeedFinalPage;

  beforeEach(() => {
    page = new Angular2SeedFinalPage();
  });

  it('should display message saying app works', () => {
    page.navigateTo();
    expect(page.getParagraphText()).toEqual('app works!');
  });
});
