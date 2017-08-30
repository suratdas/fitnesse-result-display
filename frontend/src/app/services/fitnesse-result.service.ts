import { Injectable } from '@angular/core';
import { Http, Headers } from '@angular/http';
import 'rxjs/add/operator/toPromise';
import { HttpBaseService } from "app/services/http-base.service";
import { Suite } from "app/models/Suite";


@Injectable()
export class FitnesseService extends HttpBaseService {

  http: Http;

  constructor(http: Http) {
    super(http);
  }

  public getSuites(): Promise<Suite[]> {
    return this.http.get(this.baseUrl + "/suites", this.httpOptions)
      .toPromise()
      .then(response => this.onGetSuites(response))
      .catch(this.errorHandler);
  }

  onGetSuites(response: any) {
    //console.log("Response before json:"+response);
    var data = response.json();
    //console.log("Response after json:"+data);
    return data as Suite[];
  }

  errorHandler(error: any): Promise<any> {
    console.log("An error occurred: ", error);
    return Promise.reject(error.message || error);
  }


}