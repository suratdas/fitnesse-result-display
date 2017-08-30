import { Http, Headers, RequestOptions, Response } from "@angular/http";

export class HttpBaseService {

    protected http: Http;
    protected httpOptions = new RequestOptions({ headers: new Headers({ "Content-Type": "application/json;charset=utf-8", "Accept": "applications/json" }) });
    protected baseUrl = 'http://localhost:8080/FitnesseResultDisplay/fitnesse';

    constructor(http: Http) {
        this.http = http;
    }

}