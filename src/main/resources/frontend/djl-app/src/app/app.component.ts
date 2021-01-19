import {Component, OnInit} from '@angular/core';
import {FileUploaderService} from "./service/file-uploader-service";
import {ApiService} from "./service/api-service";


@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  title = 'djl-app';
  recognizedText: string;
  imageUrl: string
  fileUploaderService: FileUploaderService;
  apiService: ApiService;

  constructor(fileUploaderService: FileUploaderService,
              apiService: ApiService) {
    this.fileUploaderService = fileUploaderService;
    this.apiService = apiService;
  }

  ngOnInit(): void {
  }

  changeFile(event) {
    this.fileUploaderService.changeFile(event);
    this.fileUploaderService.getDataUrl().then((url: string) => this.imageUrl = url);
  }

  recognize() {
    this.fileUploaderService.upload(this.apiService.handWritingRecognitionUrl())
      .then((response) => {
          this.recognizedText = response;
      })
  }
}
