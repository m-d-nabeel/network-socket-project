#include <iostream>
#include <string>
#include <vector>
using namespace std;

void GenerateTable(vector<vector<char>>& table) {
  for (int i = 0; i < 26; i++) {
    for (int j = 0; j < 26; j++) {
      table[i][j] = (i + j) % 26 + 'A';
    }
  }
}

void Encrypt(string& text, const string& key,
             const vector<vector<char>>& table) {
  cout << "Given String: " << text << endl;
  for (int k = 0; k < text.size(); k++) {
    int row = text[k] - 'A';
    int col = key[k % key.size()] - 'A';
    if (text[k] >= 'A' && text[k] <= 'Z') {
      text[k] = table[row][col];
    }
  }
  cout << "Encrypted String: " << text << endl;
}

void Decrypt(string& text, const string& key,
             const vector<vector<char>>& table) {
  cout << "Encrypted String: " << text << endl;
  for (int k = 0; k < text.size(); k++) {
    int col = key[k % key.size()] - 'A';
    if (text[k] >= 'A' && text[k] <= 'Z') {
      for (int row = 0; row < 26; row++) {
        if (table[row][col] == text[k]) {
          text[k] = row + 'A';
          break;
        }
      }
    }
  }
  cout << "Decrypted String: " << text << endl;
}

int main() {
  string text;
  cout << "Enter the string: ";
  getline(cin, text);

  string key;
  cout << "Enter the key: ";
  cin >> key;

  string temp = key;
  while (key.size() < text.size()) {
    key += temp;
  }

  vector<vector<char>> table(26, vector<char>(26));
  GenerateTable(table);

  for (int i = 0; i < text.size(); i++) {
    if (text[i] >= 'a' && text[i] <= 'z') {
      text[i] = text[i] - 'a' + 'A';
    }
  }

  for (int i = 0; i < key.size(); i++) {
    if (key[i] >= 'a' && key[i] <= 'z') {
      key[i] = key[i] - 'a' + 'A';
    }
  }

  bool flag = false;
  while (true) {
    int option;
    cout << "1. Encrypt\n2. Decrypt\n3. Exit\n";
    cin >> option;

    if (option == 1) {
      flag = true;
      Encrypt(text, key, table);
    } else if (option == 2) {
      if (!flag) {
        cout << "Please encrypt the string first." << endl;
        continue;
      }
      Decrypt(text, key, table);
      flag = false;
    } else if (option == 3) {
      break;
    } else {
      cout << "Invalid option." << endl;
    }
  }

  return 0;
}