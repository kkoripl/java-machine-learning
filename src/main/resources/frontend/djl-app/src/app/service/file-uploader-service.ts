import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";

@Injectable()
export class FileUploaderService {
  private http: HttpClient;
  private selectedFile: File;
  private fileReader: FileReader;

  constructor(http: HttpClient) {
    this.http = http;
    this.fileReader = new FileReader();
  }

  changeFile(event) {
    this.selectedFile = event.target.files[0];
  }

  getDataUrl() {
    return new Promise((resolve) => {
      this.fileReader.readAsDataURL(this.selectedFile);

      this.fileReader.onload = event => {
        var imageUrl = this.fileReader.result;
        resolve(imageUrl);
      }
    });
  }

  upload(url: string): Promise<any> {
    var uploadImageData = new FormData();
    uploadImageData.append("image", this.selectedFile, this.selectedFile.name);
    return this.http.post(url, uploadImageData).toPromise();
  }
}
