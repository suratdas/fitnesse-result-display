import { Component } from '@angular/core';
import { FitnesseService } from "app/services/fitnesse-result.service";
import { Suite } from "app/models/Suite";

@Component({
    selector: 'fitnesse-result',
    template: `
<table class='table table-striped table-bordered table-condensed'>
    <tr>
        <th>Suite Name</th>
        <th>Suite URL</th>
        <th>Enabled</th>
    </tr>
    <tr *ngFor="let suite of suites">
    <td>{{suite.suiteName}}</td>
    <td>{{suite.suiteUrl}}</td>
    <td>{{suite.shouldRun}}</td>
    </tr>
</table>
`
})

export class FitnesseResultComponent {

    suites: any[] = [];

    constructor(private fitnesseService: FitnesseService) {
    }

    ngOnInit() {
        this.fitnesseService.getSuites().then(allSuites => {
            if (allSuites != null && allSuites.length > 0) {
                for (let i = 0; i < allSuites.length; i++) {
                    this.suites.push(allSuites[i]);
                }
            }
        });
    }
}